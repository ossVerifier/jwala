package com.siemens.cto.deploy.http

/**
 * Created by z003e5zv on 4/8/2015.
 */
class TocClientForResources extends AbstractTocClient {

    public TocClientForResources(TocHttpClient tocHttpClient) {
        super(tocHttpClient, "resources")
    }

}
