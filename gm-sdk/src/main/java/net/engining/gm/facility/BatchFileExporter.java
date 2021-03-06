package net.engining.gm.facility;

import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import net.engining.gm.param.model.SystemStatus;
import net.engining.pg.batch.sdk.PgFileFacilityExporter;
import net.engining.pg.parameter.ParameterFacility;

public class BatchFileExporter extends PgFileFacilityExporter {

	@Autowired
	private ParameterFacility parameterXStreamFacility;

	private String format = "yyyyMMdd";

	@PostConstruct
	public void initBatchDate() {
		SystemStatus systemStatus = parameterXStreamFacility.getParameter(SystemStatus.class, ParameterFacility.UNIQUE_PARAM_KEY);
		setBatchNumber(new SimpleDateFormat(format).format(systemStatus.processDate));
	}
}
