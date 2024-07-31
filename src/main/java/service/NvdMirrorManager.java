package service;

import businessObjects.NvdRequestBuilder;
import businessObjects.cve.CveEntity;
import businessObjects.cve.Cve;
import businessObjects.cve.NvdMirrorMetaData;
import common.Constants;
import exceptions.ApiCallException;
import exceptions.DataAccessException;
import handlers.CveMarshaller;
import handlers.IJsonMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.IBulkDao;
import persistence.IDao;
import persistence.IMetaDataDao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

public class NvdMirrorManager {
    private final NvdApiService nvdApiService;
    private final CveResponseProcessor cveResponseProcessor;
    private final IJsonMarshaller<Cve> nvdCveMarshaller;
    private final IDao<Cve> cveDao;
    private final IDao<NvdMirrorMetaData> metadataDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirrorManager.class);

    public NvdMirrorManager(NvdApiService nvdApiService, CveResponseProcessor cveResponseProcessor, IJsonMarshaller<Cve> cveMarshaller, IDao<Cve> cveDao) {
        this.nvdApiService = nvdApiService;
        this.cveResponseProcessor = cveResponseProcessor;
        this.nvdCveMarshaller = cveMarshaller;
        this.cveDao = cveDao;
    }

    /**
     * Gets CVEs in bulk from the NVD and stores them in the given database context.
     */
    public void handleBuildMirror()throws DataAccessException, ApiCallException {
        int cveCount = 1;

        for (int i = Constants.DEFAULT_START_INDEX; i < cveCount; i += Constants.NVD_MAX_PAGE_SIZE) {
            CveEntity response = new NvdRequestBuilder()
                    .withFullMirrorDefaults(Integer.toString(i))
                    .build()
                    .executeRequest()
                    .getEntity();
            cveCount = resetCveCount(cveCount, response);
            persistPaginatedData(response, i, cveCount);
            handleSleep(i, cveCount);   // avoids hitting NVD rate limits
        }
    }

    /**
     * Handles updating the SECL NVD Mirror
     * @param lastModStartDate Timestamp of previous call to NVD CVE API to update SECL mirror
     * @param lastModEndDate Typically it is the current time - but provides the upper bound to the time window
     *                       from which to pull updates
     */
    public void handleUpdateNvdMirror(String lastModStartDate, String lastModEndDate) throws DataAccessException, ApiCallException {
        CveEntity response = new NvdRequestBuilder()
                        .withApiKey(Constants.NVD_API_KEY)
                        .withLastModStartEndDates(lastModStartDate, lastModEndDate)
                        .build()
                .executeRequest()
                .getEntity();
        persistMetadata(response);
        persistCveDetails(response);
    }

    /**
     * Handles building a full or partial NVD mirror from a json file.
     * The file must be structured in exactly the same format as a CveResponse
     *                  NVD Mirror or local containerized mongodb instance)
     * @param filepath Path to the json file formatted as a CveResponse
     * @throws DataAccessException
     */
    public void handleBuildMirrorFromJsonFile(Path filepath) throws DataAccessException {
        CveEntity fileContents = processFile(filepath);
        persistMetadata(fileContents);
        persistCveDetails(fileContents);
    }

    private int resetCveCount(int cveCount, CveEntity response) {
        return cveCount == 1
                ? cveResponseProcessor.extractTotalResults(response)
                : cveCount;
    }

    private void persistPaginatedData(CveEntity response, int loopIndex, int cveCount) throws DataAccessException {
        persistCveDetails(response);
        if(loopIndex == cveCount - 1) {
            persistMetadata(response);
        }
    }

    private void persistCveDetails(CveEntity response) throws DataAccessException {
        cveDao.insert(cveResponseProcessor.extractAllCves(response));
    }

    private void persistMetadata(CveEntity response) throws DataAccessException {
        metadataDao.update(Collections.singletonList(cveResponseProcessor.formatNvdMetaData(response)));
    }

    private void handleSleep(int startIndex, int cveCount) {
        try {
            if (startIndex != cveCount - 1) {
                Thread.sleep(Constants.DEFAULT_NVD_REQUEST_SLEEP);
            }
        } catch (InterruptedException e) {
            LOGGER.error("Thread interrupted", e);
            throw new RuntimeException(e);
        }
    }

    private CveEntity processFile(Path filepath) {
        String json = readJsonFile(filepath);
        return nvdCveMarshaller.unmarshalJson(json);
    }

    private String readJsonFile(Path filepath) {
        StringBuilder builder = new StringBuilder();
        try(Stream<String> stream = Files.lines(filepath, StandardCharsets.UTF_8)) {
            stream.forEach(s -> builder.append(s).append("\n"));
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
