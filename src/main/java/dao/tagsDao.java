package dao;

import api.ReceiptResponse;
import api.ReceiptTagResponse;
import generated.tables.Receipts;
import generated.tables.records.ReceiptTagsRecord;
import generated.tables.records.ReceiptsRecord;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static generated.Tables.RECEIPTS;
import static generated.Tables.RECEIPT_TAGS;
import java.util.stream.Collectors;


public class tagsDao {
    DSLContext dsl;

    public tagsDao(Configuration jooqConfig) {
        this.dsl = DSL.using(jooqConfig);
    }

    public void insert(String tagName, int receiptid) {
        ReceiptTagsRecord receiptTagsRecord = dsl.selectFrom(RECEIPT_TAGS)
                .where(RECEIPT_TAGS.RECEIPT_ID.eq(receiptid))
                .and(RECEIPT_TAGS.TAGNAME.eq(tagName))
                .fetchOne();

        if (receiptTagsRecord!=null){
            dsl.delete(RECEIPT_TAGS)
                    .where(RECEIPT_TAGS.RECEIPT_ID.eq(receiptid))
                    .and(RECEIPT_TAGS.TAGNAME.eq(tagName))
                    .execute();
        }

        else{
            ReceiptTagsRecord receiptsRecord2=dsl
                    .insertInto(RECEIPT_TAGS, RECEIPT_TAGS.RECEIPT_ID,RECEIPT_TAGS.TAGNAME)
                    .values(receiptid,tagName)
                    .returning(RECEIPT_TAGS.ID)
                    .fetchOne();
            checkState(receiptsRecord2 != null && receiptsRecord2.getId() != null, "Insert failed");
        }
    }

    public List<ReceiptsRecord> getAllReceiptsByTags(String tagname) {
        return dsl.selectFrom(RECEIPTS)
                .where(RECEIPTS.ID.eq(dsl
                        .select(RECEIPT_TAGS.RECEIPT_ID)
                        .from(RECEIPT_TAGS)
                        .where(RECEIPT_TAGS.TAGNAME.eq(tagname))))
                .fetch();
    }

    public List<String> getTagsByReceipt(int receiptId) {
        return dsl.selectFrom(RECEIPT_TAGS)
                .where(RECEIPT_TAGS.RECEIPT_ID.eq(receiptId)).fetch()
                .stream().map(x->x.getTagname()).collect(Collectors.toList());
    }

}
