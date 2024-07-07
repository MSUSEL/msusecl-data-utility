package presentation;

import businessObjects.cve.Cve;
import businessObjects.cve.NvdMirrorMetaData;
import common.Constants;
import exceptions.DataAccessException;
import service.NvdApiService;
import service.NvdMirrorService;

import java.time.Instant;

public class PiqueNvdMirror {
    private static final NvdApiService nvdApiService =  new NvdApiService();
    private static final NvdMirrorService nvdMirrorService = new NvdMirrorService();

    public static void buildNvdMirror(String dbContext) {
        nvdApiService.handleGetPaginatedCves(dbContext, Constants.DEFAULT_START_INDEX, Constants.NVD_MAX_PAGE_SIZE);
    }

    public static void updateNvdMirror(String dbContext) throws DataAccessException {
        NvdMirrorMetaData metadata = nvdMirrorService.handleGetCurrentMetaData(dbContext);
        Instant instant = Instant.now();
        nvdApiService.handleUpdateNvdMirror(dbContext, metadata.getTimestamp(), instant.toString());
    }

    public static NvdMirrorMetaData getCurrentMetaData(String dbContext) throws DataAccessException {
        return nvdMirrorService.handleGetCurrentMetaData(dbContext);
    }

    public static void insertSingleCve(String dbContext, Cve cve) throws DataAccessException {
        nvdMirrorService.handleInsertSingleCve(dbContext, cve);
    }
}
