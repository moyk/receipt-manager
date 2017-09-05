package controllers;

import api.CreateReceiptRequest;
import api.ReceiptResponse;
import api.ReceiptTagResponse;
import dao.tagsDao;
import generated.tables.records.ReceiptTagsRecord;
import generated.tables.records.ReceiptsRecord;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static generated.Tables.RECEIPT_TAGS;
import static java.util.stream.Collectors.toList;

@Path("/tags/{tag}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class tagsController {
    final tagsDao tags;

    public tagsController(tagsDao tags) {
        this.tags = tags;
    }

    @PUT
    public void toggleTag(@PathParam("tag") String tagName, int receiptid) {
        tags.insert(tagName,receiptid);
    }

    @GET
    public List<ReceiptResponse> getTags(@PathParam("tag") String tagName) {
        List<ReceiptsRecord> receiptsRecord = tags.getAllReceiptsByTags(tagName);
        return receiptsRecord.stream().map(ReceiptResponse::new).collect(toList());

    }
}
