package com.alphasystem.persistence.mongo.spring.support;

import com.alphasystem.persistence.mongo.model.AbstractDocument;
import com.alphasystem.persistence.mongo.model.CascadeSave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;

import static com.alphasystem.util.AppUtil.isGivenType;
import static java.lang.String.format;
import static org.springframework.util.ReflectionUtils.doWithFields;
import static org.springframework.util.ReflectionUtils.makeAccessible;

/**
 * @author sali
 */
@Component
public class CascadingMongoEventListener extends AbstractMongoEventListener {

    private MongoTemplate mongoTemplate;

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Autowired
    public void setMongoTemplate(@Qualifier("wordByWordTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void onBeforeConvert(final Object source) {
        doWithFields(source.getClass(), field -> {
            makeAccessible(field);

            if (field.isAnnotationPresent(DBRef.class) && field.isAnnotationPresent(CascadeSave.class)) {
                final Object fieldValue = field.get(source);

                DbRefFieldCallback callback = new DbRefFieldCallback();

                Class<?> fieldClass = fieldValue.getClass();

                if (Collection.class.isAssignableFrom(fieldClass)) {
                    Collection collection = (Collection) fieldValue;
                    collection.stream().filter(o -> isGivenType(AbstractDocument.class, o)).forEach(o -> {
                        save((AbstractDocument) o);
                    });
                } else {
                    doWithFields(fieldClass, callback);
                    if (!callback.isIdFound()) {
                        throw new MappingException("Cannot perform cascade save on child object without id set");
                    }
                    try {
                        mongoTemplate.save(fieldValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    /**
     * if the entity exists then use existing entity to avoid unique constraint.
     *
     * @param src
     */
    private void save(AbstractDocument src) {
        String displayName = src.getDisplayName();
        String id = src.getId();
        BasicQuery query = new BasicQuery(format("{'displayName': '%s'}", displayName));
        AbstractDocument entity = mongoTemplate.findOne(query, src.getClass());
        if (entity != null && !entity.getId().equals(id)) {
            throw new RuntimeException(format("Possible duplicate Target ID: %s, Database ID: %s, Display Name: %s",
                    id, entity.getId(), displayName));
        }
        try {
            mongoTemplate.save(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DbRefFieldCallback implements ReflectionUtils.FieldCallback {
        private boolean idFound;

        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
            makeAccessible(field);

            if (field.isAnnotationPresent(Id.class)) {
                idFound = true;
            }
        }

        public boolean isIdFound() {
            return idFound;
        }
    }
}