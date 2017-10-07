package dao;

import api.imageResponse;
import generated.tables.records.ImagesRecord;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static generated.Tables.IMAGES;

public class imageDao {
    DSLContext dsl;

    public imageDao(Configuration jooqConfig) {
        this.dsl = DSL.using(jooqConfig);
    }

    public int insert(String imageurl) {
        ImagesRecord imageRecord = dsl
                .insertInto(IMAGES, IMAGES.IMG)
                .values(imageurl)
                .returning(IMAGES.ID)
                .fetchOne();

        checkState(imageRecord != null && imageRecord.getId() != null, "Insert failed");

        return imageRecord.getId();
    }

    public List<ImagesRecord> getAllImages() {
        return dsl.selectFrom(IMAGES).fetch();
    }

}

