package api;

import generated.tables.records.ImagesRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

public class imageResponse {
    @JsonProperty
    public Integer id;

    @JsonProperty
    String img;

    public imageResponse(ImagesRecord imRecord) {
        this.id = imRecord.getId();
        this.img = imRecord.getImg();
    }
}
