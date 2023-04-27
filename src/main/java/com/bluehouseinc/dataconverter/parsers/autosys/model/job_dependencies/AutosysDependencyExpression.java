package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies;

import java.util.LinkedList;

import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util.AutosysDependencyLogicOperator;

import lombok.Data;
import lombok.EqualsAndHashCode;

// TODO: Use com.baeldung.reducingIfElse.Expression and other classes from same package as an example to model WHOLE Dependency expression!

/**
 * Some edge-case scenarios for condition expression:
 *
 * 8 statuses condition: s(BRT_TMS_6200_010.ETS_DLM_DATA_LOAD.FT00) | s(BRT_TMS_6200_020.ETS_DLM_DATA_LOAD.FT01) | s(BRT_TMS_6200_030.ETS_DLM_DATA_LOAD.FT02) |
 * s(BRT_TMS_6200_040.ETS_DLM_DATA_LOAD.FT03) | s(BRT_TMS_6200_050.ETS_DLM_DATA_LOAD.FT04) | s(BRT_TMS_6200_060.ETS_DLM_DATA_LOAD.FT05) |
 * s(BRT_TMS_6200_070.ETS_DLM_DATA_LOAD.FT06) | s(BRT_TMS_6200_080.ETS_DLM_DATA_LOAD.FT07)
 *
 * 19 statuses (io.vavr.Tuple8 will NOT work with this since only 11 pairs are missing :) -> ) condition: s(BRT_TMS_6270_010.ETS_ALL_ELIG_RSP_OUT.FT00) |
 * s(BRT_TMS_6270_020.ETS_ALL_ELIG_RSP_OUT.FT01) | s(BRT_TMS_6270_030.ETS_ALL_ELIG_RSP_OUT.FT02) | s(BRT_TMS_6270_040.ETS_ALL_ELIG_RSP_OUT.FT03) |
 * s(BRT_TMS_6270_050.ETS_ALL_ELIG_RSP_OUT.FT04) | s(BRT_TMS_6270_060.ETS_ALL_ELIG_RSP_OUT.FT05) | s(BRT_TMS_6270_060.ETS_ALL_ELIG_RSP_OUT.FT06) |
 * s(BRT_TMS_6270_060.ETS_ALL_ELIG_RSP_OUT.FT07) | s(BRT_TMS_6270_060.ETS_ALL_ELIG_RSP_OUT.FT08) | s(BRT_TMS_6270_060.ETS_ALL_ELIG_RSP_OUT.FT09) |
 * s(BRT_TMS_6270_060.ETS_ALL_ELIG_RSP_OUT.FT10) | s(BRT_TMS_6270_060.ETS_ALL_ELIG_RSP_OUT.FT11) | s(BRT_TMS_6270_061.ETS_ALL_ELIG_RSP_OUT.FT12) |
 * s(BRT_TMS_6270_062.ETS_ALL_ELIG_RSP_OUT.FT13) | s(BRT_TMS_6270_063.ETS_ALL_ELIG_RSP_OUT.FT14) | s(BRT_TMS_6270_064.ETS_ALL_ELIG_RSP_OUT.FT15) |
 * s(BRT_TMS_6270_065.ETS_ALL_ELIG_RSP_OUT.FT16) | s(BRT_TMS_6270_066.ETS_ALL_ELIG_RSP_OUT.FT17) | s(BRT_TMS_6270_067.ETS_ALL_ELIG_RSP_OUT.FT18)
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AutosysDependencyExpression {
	// this can be any dependency type (Variable, ExitCode, JobStatus)
	AutosysBaseDependency baseDependency; // this can be any dependency type (Variable, ExitCode, JobStatus)
	AutosysDependencyLogicOperator logicOperator; // &, |, and, or
	LinkedList<AutosysDependencyExpression> expression; // Because we don't want to set LinkedList for single object, but for nested expression
	// AutosysDependencyExpression <=> Node
	AutosysDependencyExpression autosysDependencyExpression; // Points to another Node if there is any LogicalOperator after baseDependency.
	// E.g. in expression `s(jobName1) & d(jobName2)`, where  `s(jobName1)` is baseDependency and `d(jobName2)` is node which it points to.

}
