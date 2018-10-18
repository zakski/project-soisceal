package org.springframework.core.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StringResource extends AbstractResource {

    public final String value;

    public StringResource(String s ){
        this.value = s;
    }

   @Override
    public String getDescription() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(value.getBytes());
    }
}
