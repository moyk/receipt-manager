package api;

import com.fasterxml.jackson.annotation.JsonProperty;
import generated.tables.records.ReceiptTagsRecord;

public class ReceiptTagResponse {
    @JsonProperty
    Integer id;

    @JsonProperty
    String tagname;

    @JsonProperty
    Integer receipt_id;

    public ReceiptTagResponse(ReceiptTagsRecord dbRecord) {
        this.tagname = dbRecord.getTagname();
        this.id = dbRecord.getId();
        this.receipt_id =dbRecord.getReceiptId();
    }
}
