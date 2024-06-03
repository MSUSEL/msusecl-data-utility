package api;

import data.cveData.CVEResponse;
import api.baseClasses.BaseResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Object representation of an NVD response
 * header fields inherited from base class
 */
@Getter
@Setter
public class NVDResponse extends BaseResponse {
    private CVEResponse cveResponse;
}

