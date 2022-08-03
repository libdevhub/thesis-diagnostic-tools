package teza;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.MarcReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.SubfieldImpl;

public class TezaParser {

	
	public static void main(String[] args) {
		
		final File file = new File("C:\\\\Users\\\\ajacobsmo\\\\Desktop\\\\תזות\\\\עדכון תזות עם כלי אבחון\\\\result" + System.currentTimeMillis() + ".xml");

		File tezaFile = new File("C:\\Users\\ajacobsmo\\Desktop\\תזות\\עדכון תזות עם כלי אבחון\\Theses bib records (with psytests).xml");
		File toolsFile = new File("C:\\Users\\ajacobsmo\\Desktop\\תזות\\עדכון תזות עם כלי אבחון\\Psytests with 773-thesis.xml");
		
		try {
			MarcReader tezaReader = new MarcXmlReader(new FileInputStream(tezaFile));
			   
			MarcWriter writer = new MarcXmlWriter(new FileOutputStream(file));
			    
			
			while (tezaReader.hasNext()) {
				MarcReader toolsReader = new MarcXmlReader(new FileInputStream(toolsFile));
		        Record tezaRecord = tezaReader.next();
		        Record extendedRecord = tezaRecord;
		        ControlField tezaId =  (ControlField)tezaRecord.getVariableField("001");
		        if (tezaId.getData().equalsIgnoreCase("9919670474902791")) {
		        	System.out.println("9919670474902791");
		        }
		        List<VariableField> diagnosticTools = tezaRecord.getVariableFields("505");
		        //Remove from list indicator1 diff from 2
		        List<VariableField> listToRemove = new ArrayList<VariableField>();
		        for (int d=0; d<diagnosticTools.size(); d++) { 
		        	DataField diagnosticTool = (DataField)diagnosticTools.get(d);
		        	if (diagnosticTool.getIndicator1() != '2' || diagnosticTool.getIndicator2() != ' ') {
		        		listToRemove.add(diagnosticTool);
		        	}
		        }
		        diagnosticTools.removeAll(listToRemove);
		        String tezaIdStr = tezaId.getData();
		        while (toolsReader.hasNext()) {
		        	Record toolRecord = toolsReader.next();
			        DataField tezaIdInTool =  (DataField)toolRecord.getVariableField("773");
			        List<Subfield> sf = tezaIdInTool!=null ? tezaIdInTool.getSubfields('w') : new ArrayList<Subfield>();
			        String dataStr = sf!= null && sf.size()>0 ? sf.get(0).getData() : "";
			        if (tezaIdStr.equals(dataStr)) {
			        	DataFieldImpl toolsField = null;//new DataFieldImpl("505", '2', ' ');
//			        	List<DataField> dataFieldsToAddTo = extendedRecord.getDataFields() != null ? extendedRecord.getDataFields() : new ArrayList<DataField>();
			        	
			        	StringBuilder allFieldsWithSeperator = new StringBuilder();
			        	List<VariableField> allfields = toolRecord.getVariableFields("245");
			        	for (int i=0; i<allfields.size(); i++) { 
			        		DataField tFieldFromTools = (DataField)allfields.get(i);
			        		List<Subfield> tFieldFromToolsSubFields = tFieldFromTools != null ? tFieldFromTools.getSubfields('a') : new ArrayList<Subfield>();
			        		allFieldsWithSeperator.append(tFieldFromToolsSubFields.get(0).getData());
			        		allFieldsWithSeperator.append("##");
			        	}
//			        	DataField tFieldFromTools = (DataField)toolRecord.getVariableFields("245");
//			        	List<Subfield> tFieldFromToolsSubFields = tFieldFromTools != null ? tFieldFromTools.getSubfields('a') : new ArrayList<Subfield>();
//			       
			        	//StringBuilder allFieldsWithSeperator = new StringBuilder();
//				   		for (int f=0; f < tFieldFromToolsSubFields.size(); f++) { 
//				   			allFieldsWithSeperator.append(tFieldFromToolsSubFields.get(f).getData());
//				   			allFieldsWithSeperator.append("##");
//				   			if(f>0) {
//				   				System.out.println("here");
//				   			}
//				   		}
				   		String s = allFieldsWithSeperator.length() > 0 ? allFieldsWithSeperator.substring(0, allFieldsWithSeperator.lastIndexOf("##")) : "";
				   		if (allfields.size() > 1 || diagnosticTools == null || diagnosticTools.size() == 0) {
				   			toolsField = new DataFieldImpl("505", '2', ' ');
				   		}
				   		
			        	SubfieldImpl tSubField = new SubfieldImpl('t', s);
			        	if(tSubField != null && toolsField != null) {
			        		toolsField.addSubfield(tSubField);
			        	}
			        	
//			        	ControlField eightFieldFromTools = (ControlField)toolRecord.getVariableField("001");
//			        	SubfieldImpl eightSubField = eightFieldFromTools != null ? new SubfieldImpl('8', eightFieldFromTools.getData()) : null;
//			        	if(eightSubField != null && toolsField != null) {
//			        		toolsField.addSubfield(eightSubField);
//			        	}
			        	
			        	DataField gFieldFromTools = (DataField)toolRecord.getVariableField("300");
			        	List<Subfield> gFieldFromToolsSubFields = gFieldFromTools != null ? gFieldFromTools.getSubfields('a') : new ArrayList<Subfield>();
			        	SubfieldImpl gSubField = gFieldFromToolsSubFields != null && gFieldFromToolsSubFields.size()>0 ? new SubfieldImpl('g', gFieldFromToolsSubFields.get(0).getData()) : null;
			        	if(gSubField != null) {
			        		//System.out.println(gSubField.getData());
			        		Pattern p = Pattern.compile("\\d+(?:-\\d+)?");
			                Matcher m = p.matcher(gSubField.getData());
			                String pageNumber = "";
			                while(m.find()) {
			                	pageNumber = m.group();
			                    //System.out.println("From matcher!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + pageNumber);
			                    if (pageNumber.contains("-")) {
			                    	break;
			                    }
			                    try {
			                    	if (Integer.parseInt(pageNumber) > 5) {
			                        	break;
			                        }
								} catch (Exception NumberFormatException) {
									continue;
								}
			                }
			        		pageNumber = pageNumber != "" ?pageNumber : gSubField.getData();
			        		gSubField.setData(pageNumber);
			        		if (toolsField != null) {
			        			toolsField.addSubfield(gSubField);
			        		}
			        	}
			        	
//			        	dataFieldsToAddTo.add(toolsField);
			        	if (toolsField != null) {
			        		extendedRecord.addVariableField(toolsField);
			        	}
			        	else {
			        		boolean match = false;
			        		for (int j=0; j<diagnosticTools.size(); j++) { 
				        		DataField diagnosticTool = (DataField)diagnosticTools.get(j);
				        		List<Subfield> diagnosticToolSubFields = diagnosticTool != null ? diagnosticTool.getSubfields('a') : new ArrayList<Subfield>();
				        		if(diagnosticToolSubFields != null && diagnosticToolSubFields.size() > 0 && s.equalsIgnoreCase(diagnosticToolSubFields.get(0).getData())) {
				        			match = true;
				        			if (tSubField != null) {
				        				diagnosticTool.removeSubfield(diagnosticToolSubFields.get(0));
				        				diagnosticTool.addSubfield(tSubField);
				        			}
//				        			if(eightSubField != null) {
//				        				diagnosticTool.addSubfield(eightSubField);
//				        			}
				        			if(gSubField != null) {
				        				diagnosticTool.addSubfield(gSubField);
				        			}
				        			break;
				        		}
				        		
				        	} 
			        		
			        		if (!match) {
			        			toolsField = new DataFieldImpl("505", '2', ' ');
			        			if (tSubField != null) {
			        				toolsField.addSubfield(tSubField);
			        			}
//			        			if(eightSubField != null) {
//			        				toolsField.addSubfield(eightSubField);
//			        			}
			        			if(gSubField != null) {
			        				toolsField.addSubfield(gSubField);
			        			}
			        			extendedRecord.addVariableField(toolsField);
			        		}
			        	}
			        	
			        }
		        }
		        writer.write(extendedRecord);
		        
		    }
			writer.close();
			System.out.println("finished");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("File not found");
		}

	}

}
