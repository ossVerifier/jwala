package com.cerner.jwala.files;

import java.io.IOException;

import com.cerner.jwala.common.request.app.RemoveWebArchiveRequest;
import com.cerner.jwala.common.request.app.UploadWebArchiveRequest;

public interface WebArchiveManager {

    RepositoryFileInformation store(UploadWebArchiveRequest uploadWebArchiveRequest) throws IOException;

    RepositoryFileInformation remove(RemoveWebArchiveRequest removeWebArchiveRequest) throws IOException;

}
