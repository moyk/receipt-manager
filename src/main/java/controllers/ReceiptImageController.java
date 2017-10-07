package controllers;

import api.ReceiptSuggestionResponse;
import api.imageResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import dao.imageDao;
import generated.tables.records.ImagesRecord;
import org.hibernate.validator.constraints.NotEmpty;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.toList;

@Path("/images")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptImageController {
    private final AnnotateImageRequest.Builder requestBuilder;
    private final imageDao imgdao;

    public ReceiptImageController(imageDao imgDao) {
        // DOCUMENT_TEXT_DETECTION is not the best or only OCR method available
        Feature ocrFeature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        this.requestBuilder = AnnotateImageRequest.newBuilder().addFeatures(ocrFeature);
        this.imgdao = imgDao;

    }

    public static String imgToBase64String(final RenderedImage img, final String formatName) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, formatName, Base64.getEncoder().wrap(os));
            return os.toString(StandardCharsets.ISO_8859_1.name());
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static BufferedImage base64StringToImg(final String base64String) {
        try {
            return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private static BufferedImage cropImage(BufferedImage src, int x, int y, int w, int h) {
        return src.getSubimage(x, y, w, h);
    }

    private static String getThumbnail(BufferedImage src, List<EntityAnnotation> textAnnotations) {
        int upperX = Integer.MAX_VALUE;
        int upperY = Integer.MAX_VALUE;
        int lowerX = Integer.MIN_VALUE;
        int lowerY = Integer.MIN_VALUE;
        for (EntityAnnotation annotation : textAnnotations) {
            upperX = Integer.min(upperX, annotation.getBoundingPoly().getVertices(0).getX());
            upperX = Integer.min(upperX, annotation.getBoundingPoly().getVertices(3).getX());
            upperY = Integer.min(upperY, annotation.getBoundingPoly().getVertices(0).getY());
            upperY = Integer.min(upperY, annotation.getBoundingPoly().getVertices(1).getY());

            lowerX = Integer.max(lowerX, annotation.getBoundingPoly().getVertices(1).getX());
            lowerX = Integer.max(lowerX, annotation.getBoundingPoly().getVertices(2).getX());
            lowerY = Integer.max(lowerY, annotation.getBoundingPoly().getVertices(2).getY());
            lowerY = Integer.max(lowerY, annotation.getBoundingPoly().getVertices(3).getY());
        }

        BufferedImage thumbnail = cropImage(src, upperX, upperY, lowerX - upperX, lowerY - upperY);
        return imgToBase64String(thumbnail, "png");
    }

    /**
     * This borrows heavily from the Google Vision API Docs.  See:
     * https://cloud.google.com/vision/docs/detecting-fulltext
     * <p>
     * YOU SHOULD MODIFY THIS METHOD TO RETURN A ReceiptSuggestionResponse:
     * <p>
     * public class ReceiptSuggestionResponse {
     * String merchantName;
     * String amount;
     * }
     */
    @POST
    public ReceiptSuggestionResponse parseReceipt(@NotEmpty String base64EncodedImage) throws Exception {
        Image img = Image.newBuilder().setContent(ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedImage))).build();
        AnnotateImageRequest request = this.requestBuilder.setImage(img).build();
        BufferedImage originImg = base64StringToImg(base64EncodedImage);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse responses = client.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = responses.getResponses(0);

            String merchantName = null;
            BigDecimal amount = null;

            // Your Algo Here!!
            // Sort text annotations by bounding polygon.  Top-most non-decimal text is the merchant
            // bottom-most decimal text is the total amount
            List<EntityAnnotation> entityList = new ArrayList<EntityAnnotation>();
            for (EntityAnnotation annotation : res.getTextAnnotationsList()) {

                entityList.add(annotation);
            }
            String[] split = entityList.get(0).getDescription().split("\\n");

            String[] merchantSplit = split[0].split("(?<=\\D)(?=\\d)");

            List<Double> amountList = new ArrayList<>();
            for (int i = split.length - 1; i >= 0; i--) {

                Pattern p = Pattern.compile("(-?[0-9]+(?:[,.][0-9]+)?)");
                Matcher m = p.matcher(split[i]);

                while (m.find()) {
                    amountList.add(Double.valueOf(m.group()));
                }
            }
            merchantName = merchantSplit[0];
            amount = BigDecimal.valueOf(amountList.get(0));
            //TextAnnotation fullTextAnnotation = res.getFullTextAnnotation();

            String thumbnail = getThumbnail(originImg, entityList);
            imgdao.insert(thumbnail);
            return new ReceiptSuggestionResponse(merchantName, amount, thumbnail);
        }
    }

    @GET
    public List<imageResponse> getImages() {
        List<ImagesRecord> receiptRecords = imgdao.getAllImages();
        List<imageResponse> response = receiptRecords.stream().map(imageResponse::new).collect(toList());
        return response;
    }
}
