package com.csrd.pims.service.lambda;

import java.io.InputStream;

public interface AnsMsgHandlerInterface {
    void actMsg(InputStream is, String line);
}
