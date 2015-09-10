/**
 * 
 */
package com.alphasystem.persistence.mongo.repository;

import com.alphasystem.persistence.mongo.model.AbstractDocument;
import org.springframework.data.mongodb.core.mapping.event.*;

/**
 * @author sali
 * 
 */
public abstract class DocumentEventListener<T extends AbstractDocument> extends
		AbstractMongoEventListener<T> {


    @Override
    public void onAfterConvert(AfterConvertEvent<T> event) {
        super.onAfterConvert(event);
    }


    @Override
    public void onAfterDelete(AfterDeleteEvent<T> event) {
        super.onAfterDelete(event);
    }

    @Override
    public void onAfterLoad(AfterLoadEvent<T> event) {
        super.onAfterLoad(event);
    }

    @Override
    public void onAfterSave(AfterSaveEvent<T> event) {
        super.onAfterSave(event);
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<T> event) {
        super.onBeforeConvert(event);
        T source = event.getSource();
        source.initDisplayName();
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<T> event) {
        super.onBeforeDelete(event);
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<T> event) {
        super.onBeforeSave(event);
    }

}
