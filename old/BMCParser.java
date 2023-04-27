package com.dataconverter.parsers.xml;

import com.dataconverter.domainmodel.*;
import com.dataconverter.model.IBaseJobGroupObject;
import com.dataconverter.model.impl.JobGroup;
import com.dataconverter.model.impl.TidalDataModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BMCParser extends AbstractXmlParser {

    @Override
    public TidalDataModel parseFile(String filePath) throws Exception {
        return this.parseXmlFile(filePath);
    }


    public TidalDataModel parseXmlFile(String filePath) throws Exception {
        Document document = this.readXmlFile(filePath);

        TidalDataModel dataModel = new TidalDataModel();

        NodeList container = document.getElementsByTagName("DEFTABLE");

        this.traverse(dataModel, container.item(0), null, null);

        return dataModel;
    }

    @Override
    public void onNodeExplored(TidalDataModel dataModel, Node node, IBaseJobGroupObject currentFolder, IBaseJobGroupObject currentJob) throws Exception {
        String nodeName = node.getNodeName();
        switch (nodeName){
            case "FOLDER":
            case "SMART_FOLDER":
            case "SUB_FOLDER":
                Folder folder = this.assembleFolder(node, currentFolder);

                if(currentFolder != null) {
                    currentFolder.addChild(folder);
                } else {
                    // commented out as it breaks now
//                    dataModel.getFolders().add(folder);
                }

                if (node.hasChildNodes()) {
                    traverse(dataModel, node.getChildNodes(), folder, null);
                }
                break;

            case "JOB":
                Job job = assembleJob(node);

                if(currentFolder != null) {
                    currentFolder.addChild(job);
                } else {
                    // commented out as it breaks now
//                    dataModel.getJobs().add(job);
                }

                if (node.hasChildNodes()) {
                    traverse(dataModel, node.getChildNodes(), currentFolder, job);
                }
                break;

//            case "INCOND":
//            case "OUTCOND":
//                Cond cond = this.assembleCond(node);
//                if(currentJob != null) {
//                    currentJob.getConditions().add(cond);
//                } else if(currentFolder != null) {
//                    currentFolder.getConditions().add(cond);
//                }
//                break;

            default:
//                System.out.println(String.format("Unknown node name %s", nodeName));
        }
    }

    private JobGroup assembleFolder(Node node, IBaseJobGroupObject parentFolder) {
    	JobGroup folder = new JobGroup();

        folder.setId(Integer.parseInt(this.getAttributeByName(node.getAttributes(), "REAL_FOLDER_ID")));
        folder.setName(this.getAttributeByName(node.getAttributes(), "FOLDER_NAME"));
        if(parentFolder != null) {
            folder.setParent(parentFolder);
        }

        return folder;
    }

    private Job assembleJob(Node node) {
        Job job = new Job();

        job.setId(Integer.parseInt(this.getAttributeByName(node.getAttributes(), "JOBISN")));
        job.setName(this.getAttributeByName(node.getAttributes(), "JOBNAME"));
        job.setNotes(this.getAttributeByName(node.getAttributes(), "DESCRIPTION"));

        return job;
    }

    private Cond assembleCond(Node node) {
        Cond cond = new Cond();

//        cond.setId(this.getUuid());
//        cond.setName(this.getAttributeByName(node.getAttributes(), "NAME"));
//        cond.setType(this.detectCondType(node.getNodeName()));

        return cond;
    }

    private CondType detectCondType(String name){
        switch (name){
            case "INCOND":
                return CondType.IN;

            case "OUTCOND":
                return CondType.OUT;

            default:
                return CondType.UNKNOWN;
        }
    }
}
