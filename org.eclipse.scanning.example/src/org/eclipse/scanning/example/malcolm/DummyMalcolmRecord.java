package org.eclipse.scanning.example.malcolm;

import java.util.HashMap;
import java.util.Map;

import org.epics.pvaccess.server.rpc.RPCResponseCallback;
import org.epics.pvaccess.server.rpc.RPCServiceAsync;
import org.epics.pvaccess.server.rpc.Service;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;

class DummyMalcolmRecord extends PVRecord {
	
	// Static Data
    private static final FieldCreate FIELDCREATE   = FieldFactory.getFieldCreate();
    private static final PVDataCreate PVDATACREATE = PVDataFactory.getPVDataCreate();
    private static final String CORE_ID            = "malcolm:core/";
    private static final String STATEVALUE         = "state.value";

    // Member data
    private boolean                  underControl     = false;
    private Map<String, PVStructure> receivedRPCCalls = new HashMap<String, PVStructure>();

	public Map<String, PVStructure> getReceivedRPCCalls() {
		return receivedRPCCalls;
	}

	synchronized boolean takeControl() {
        if (!underControl) {
            underControl = true;
            return true;
        }
        return false;
    }

    synchronized void releaseControl() {
        underControl = false;
    }

    private class RPCServiceAsyncImpl implements RPCServiceAsync {

        private DummyMalcolmRecord pvRecord;
        private final Status statusOk = StatusFactory.
                getStatusCreate().getStatusOK();
        private String methodName = "";

        RPCServiceAsyncImpl(DummyMalcolmRecord record, String methodName) {
            pvRecord = record;
            this.methodName = methodName;
        }

        public void request(PVStructure args, RPCResponseCallback callback)
        {
        	System.out.println("Got Async Request:");
        	System.out.println(args.toString());
        	receivedRPCCalls.put(methodName, args);
        	
            boolean haveControl = pvRecord.takeControl();
            if (!haveControl)
            {
                handleError("Device busy", callback, haveControl);
                return;
            }
            

            Structure mapStructure = FIELDCREATE.createFieldBuilder().
        			setId(CORE_ID+"Map:1.0").
        			createStructure();
            PVStructure returnPvStructure = PVDATACREATE.createPVStructure(mapStructure);
            
            if ("validate".equals(methodName)) {
            	returnPvStructure = args;
            } else if ("configure".equals(methodName)) {
            	pvRecord.getPVStructure().getSubField(PVString.class, STATEVALUE).put("CONFIGURING");
            } else if ("run".equals(methodName)) {
                pvRecord.getPVStructure().getSubField(PVString.class, STATEVALUE).put("RUNNING");
                try {
    				Thread.sleep(2000);
    			} catch (InterruptedException e1) {
    				e1.printStackTrace();
    			}
            }


        	pvRecord.getPVStructure().getSubField(PVString.class, STATEVALUE).put("ARMED");
            
        	pvRecord.releaseControl();
            callback.requestDone(statusOk, returnPvStructure);
            return;
            
        }

        private void handleError(String message, RPCResponseCallback callback, boolean haveControl)
        {
            if (haveControl)
                pvRecord.releaseControl();
            Status status = StatusFactory.getStatusCreate().
                    createStatus(StatusType.ERROR, message, null);
            callback.requestDone(status, null);
        }
    }

    public static DummyMalcolmRecord create(String recordName)
    {
        FieldBuilder fb = FIELDCREATE.createFieldBuilder();
        
        final String description = "description";
        final String tags        = "tags";
        final String writeable   = "writeable";
        final String label       = "label";
        final String labels      = "labels";
        final String dtype       = "dtype";
        final String meta        = "meta";
        final String value       = "value";
        final String eid         = "epics:nt/NTScalar:1.0";
        final String name        = "name";
        final String names       = "names";
        final String x           = "x";
        final String y           = "y";
        final String visible     = "visible";
        final String detector    = "detector";
        final String filename    = "filename";
        final String dataset     = "dataset";
        final String users       = "users";
        final String units       = "units";
        final String scale       = "scale";
        final String centre      = "centre";
        final String radius      = "radius";
                      
        Structure metaStructure = FIELDCREATE.createFieldBuilder().
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"BlockMeta:1.0").
    			createStructure();
        
        Structure choiceMetaStructure = fb.
    			add(description, ScalarType.pvString).
    			addArray("choices", ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"ChoiceMeta:1.0").
                createStructure();
        
        Structure healthMetaStructure = FIELDCREATE.createFieldBuilder().
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"HealthMeta:1.0").
    			createStructure();
        
        Structure booleanMetaStructure = FIELDCREATE.createFieldBuilder().
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"BooleanMeta:1.0").
    			createStructure();
        
        Structure intNumberMetaStructure = FIELDCREATE.createFieldBuilder().
    			add(dtype, ScalarType.pvString).
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"NumberMeta:1.0").
    			createStructure();
        
        Structure floatNumberMetaStructure = FIELDCREATE.createFieldBuilder().
    			add(dtype, ScalarType.pvString).
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"NumberMeta:1.0").
    			createStructure();
        
        Structure stringArrayMetaStructure = FIELDCREATE.createFieldBuilder().
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"StringArrayMeta:1.0").
    			createStructure();
        
        Structure numberArrayMetaStructure = FIELDCREATE.createFieldBuilder().
    			add(dtype, ScalarType.pvString).
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"NumberArrayMeta:1.0").
    			createStructure();

        Structure mapMetaStructure = FIELDCREATE.createFieldBuilder().
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			addArray("required", ScalarType.pvString).
    			setId(CORE_ID+"MapMeta:1.0").
    			createStructure();
        
        Structure tableElementsStructure = FIELDCREATE.createFieldBuilder().
    			add(detector, stringArrayMetaStructure).
    			add(filename, stringArrayMetaStructure).
    			add(dataset, stringArrayMetaStructure).
    			add(users, numberArrayMetaStructure).
    			createStructure();
        
        Structure tableMetaStructure = FIELDCREATE.createFieldBuilder().
    			add("elements", tableElementsStructure).
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"TableMeta:1.0").
    			createStructure();
        
        Structure pointGeneratorMetaStructure = FIELDCREATE.createFieldBuilder().
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			setId(CORE_ID+"PointGeneratorMeta:1.0").
    			createStructure();
        
        // Attributes
        Structure choiceStructure = FIELDCREATE.createFieldBuilder().
    			add(meta, choiceMetaStructure).
    			add(value, ScalarType.pvString).
    			setId(eid).
    			createStructure();
        
        Structure healthStructure = FIELDCREATE.createFieldBuilder().
    			add(meta, healthMetaStructure).
    			add(value, ScalarType.pvString).
    			setId(eid).
    			createStructure();
        
        Structure stringArrayStructure = FIELDCREATE.createFieldBuilder().
    			add(meta, stringArrayMetaStructure).
    			addArray(value, ScalarType.pvString).
    			setId("epics:nt/NTScalarArray:1.0").
    			createStructure();
        
        Structure booleanStructure = FIELDCREATE.createFieldBuilder().
    			add(meta, booleanMetaStructure).
    			add(value, ScalarType.pvBoolean).
    			setId(eid).
    			createStructure();
        
        Structure intStructure = FIELDCREATE.createFieldBuilder().
    			add(meta, intNumberMetaStructure).
    			add(value, ScalarType.pvInt).
    			setId(eid).
    			createStructure();
        
        Structure datasetTableValueStructure = FIELDCREATE.createFieldBuilder().
    			addArray(detector, ScalarType.pvString).
    			addArray(filename, ScalarType.pvString).
    			addArray(dataset, ScalarType.pvString).
    			addArray(users, ScalarType.pvInt).
    			createStructure();
        
        Structure datasetTableStructure = FIELDCREATE.createFieldBuilder().
    			add(meta, tableMetaStructure).
    			addArray(labels, ScalarType.pvString).
    			add(value, datasetTableValueStructure).
    			setId("epics:nt/NTTable:1.0").
    			createStructure();
        
        Structure layoutTableValueStructure = FIELDCREATE.createFieldBuilder().
    			addArray(name, ScalarType.pvString).
    			addArray("mri", ScalarType.pvString).
    			addArray(x, ScalarType.pvFloat).
    			addArray(y, ScalarType.pvFloat).
    			addArray(visible, ScalarType.pvBoolean).
    			createStructure();
        
        Structure layoutTableStructure = FIELDCREATE.createFieldBuilder().
    			add(meta, tableMetaStructure).
    			addArray(labels, ScalarType.pvString).
    			add(value, layoutTableValueStructure).
    			setId("epics:nt/NTTable:1.0").
    			createStructure();
        
        Structure methodStructure = FIELDCREATE.createFieldBuilder().
    			add("takes", mapMetaStructure).
    			add(description, ScalarType.pvString).
    			addArray(tags, ScalarType.pvString).
    			add(writeable, ScalarType.pvBoolean).
    			add(label, ScalarType.pvString).
    			add("returns", mapMetaStructure).
    			setId(CORE_ID+"MethodMeta:1.0").
    			createStructure();
        
        Structure floatStructure = FIELDCREATE.createFieldBuilder().
    			add(meta, floatNumberMetaStructure).
    			add(value, ScalarType.pvFloat).
    			setId(eid).
    			createStructure();

        Union union = FieldFactory.getFieldCreate().createVariantUnion();
		Structure generatorStructure = FieldFactory.getFieldCreate().createFieldBuilder().
    			addArray("mutators", union).
    			addArray("generators", union).
    			addArray("excluders", union).
    			setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();
		
		Structure spiralGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder().
    			addArray(centre, ScalarType.pvDouble).
    			add(scale, ScalarType.pvDouble).
    			add(units, ScalarType.pvString).
    			addArray(names, ScalarType.pvString).
    			add("alternate_direction", ScalarType.pvBoolean).
    			add(radius, ScalarType.pvDouble).
    			setId("scanpointgenerator:generator/SpiralGenerator:1.0").
				createStructure();
		
		Structure pointGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder().
    			add(meta, pointGeneratorMetaStructure).
    			add(value, generatorStructure).
    			setId(CORE_ID+"PointGenerator:1.0").
				createStructure();
        
        // Device
        Structure deviceStructure = fb.
                add(meta, metaStructure).
                add("state", choiceStructure).
                add("health", healthStructure).
                add("busy", booleanStructure).
                add("totalSteps", intStructure).
                add("abort", methodStructure).
                add("configure",methodStructure).
                add("disable", methodStructure).
                add("reset", methodStructure).
                add("run", methodStructure).
                add("validate", methodStructure).
                add("A", floatStructure).
                add("B", floatStructure).
                add("axesToMove", stringArrayStructure).
                add("layout", layoutTableStructure).
                add("datasets", datasetTableStructure).
                add("generator", pointGeneratorStructure).
                add("completedSteps", intStructure).
    			setId(CORE_ID+"Block:1.0").
                createStructure();
        
        PVStructure blockPVStructure = PVDATACREATE.createPVStructure(deviceStructure);
        
     // State
		String[] choicesArray = new String[] {"Resetting","Ready","Armed","Configuring","Running","PostRun","Paused","Rewinding","Aborting","Aborted","Fault","Disabling","Disabled"};

		PVStringArray choices = blockPVStructure.getSubField(PVStringArray.class, "state.meta.choices");
		choices.put(0, choicesArray.length, choicesArray, 0);
		
        blockPVStructure.getSubField(PVString.class, STATEVALUE).put("READY");
        
        // Health
        blockPVStructure.getSubField(PVString.class, "health.value").put("Test Health");
        
        // Busy
        blockPVStructure.getSubField(PVBoolean.class, "busy.value").put(false);
        
        // Total Steps
        blockPVStructure.getSubField(PVInt.class, "totalSteps.value").put(123);
        
        // A
        blockPVStructure.getSubField(PVFloat.class, "A.value").put(0.0f);
        
        // B
        blockPVStructure.getSubField(PVFloat.class, "B.value").put(5.2f);
        blockPVStructure.getSubField(PVBoolean.class, "B.meta.writeable").put(true);
        
        // axes
		String[] axesArray = new String[] {x,y};

		PVStringArray axes = blockPVStructure.getSubField(PVStringArray.class, "axesToMove.value");
		axes.put(0, axesArray.length, axesArray, 0);
        
        // datasets
		PVStructure datasetsPVStructure = blockPVStructure.getStructureField("datasets");
		String[] detectorArray = new String[] {"panda2", "panda2", "express3"};
		String[] filenameArray = new String[] {"panda2.h5", "panda2.h5", "express3.h5"};
		String[] datasetArray = new String[] {"/entry/detector/I200", "/entry/detector/Iref", "/entry/detector/det1"};
		int[] usersArray = new int[] {3, 1, 42};
		PVStructure tableValuePVStructure = datasetsPVStructure.getStructureField(value);
		tableValuePVStructure.getSubField(PVStringArray.class, detector).put(0, detectorArray.length, detectorArray, 0);
		tableValuePVStructure.getSubField(PVStringArray.class, filename).put(0, filenameArray.length, filenameArray, 0);
		tableValuePVStructure.getSubField(PVStringArray.class, dataset).put(0, datasetArray.length, datasetArray, 0);
		tableValuePVStructure.getSubField(PVIntArray.class, users).put(0, usersArray.length, usersArray, 0);
		String[] headingsArray = new String[] {detector, filename, dataset, users};
		datasetsPVStructure.getSubField(PVStringArray.class, labels).put(0, headingsArray.length, headingsArray, 0);
		
		// current step
        blockPVStructure.getSubField(PVInt.class, "completedSteps.value").put(1);
        
        // layout
		PVStructure layoutPVStructure = blockPVStructure.getStructureField("layout");
		String[] layoutNameArray = new String[] {"BRICK", "MIC", "ZEBRA"};
		String[] layoutMrifilenameArray = new String[] {"P45-BRICK01", "P45-MIC", "P45-ZEBRA01"};
		float[] layoutXArray = new float[] {0.0f, 0.0f, 0.0f};
		float[] layoutYArray = new float[] {0.0f, 0.0f, 0.0f};
		boolean[] layoutVisibleArray = new boolean[] {false, false, false};
		PVStructure layoutTableValuePVStructure = layoutPVStructure.getStructureField(value);
		layoutTableValuePVStructure.getSubField(PVStringArray.class, name).put(0, layoutNameArray.length, layoutNameArray, 0);
		layoutTableValuePVStructure.getSubField(PVStringArray.class, "mri").put(0, layoutMrifilenameArray.length, layoutMrifilenameArray, 0);
		layoutTableValuePVStructure.getSubField(PVFloatArray.class, x).put(0, layoutXArray.length, layoutXArray, 0);
		layoutTableValuePVStructure.getSubField(PVFloatArray.class, y).put(0, layoutYArray.length, layoutYArray, 0);
		layoutTableValuePVStructure.getSubField(PVBooleanArray.class, visible).put(0, layoutVisibleArray.length, layoutVisibleArray, 0);
		String[] layoutHeadingsArray = new String[] {name, "mri", x, y, visible};
		layoutPVStructure.getSubField(PVStringArray.class, labels).put(0, layoutHeadingsArray.length, layoutHeadingsArray, 0);
        

		
        PVStructure spiralGeneratorPVStructure = PVDataFactory.getPVDataCreate().createPVStructure(spiralGeneratorStructure);
		double[] acentre = new double[]{3.5, 4.5};
		spiralGeneratorPVStructure.getSubField(PVDoubleArray.class, centre).put(0, acentre.length, acentre, 0);
		spiralGeneratorPVStructure.getDoubleField(scale).put(1.5);
		spiralGeneratorPVStructure.getStringField(units).put("mm");
		String[] anames = new String[]{x, y};
		spiralGeneratorPVStructure.getSubField(PVStringArray.class, names).put(0, anames.length, anames, 0);
		spiralGeneratorPVStructure.getBooleanField("alternate_direction").put(true);
		spiralGeneratorPVStructure.getDoubleField(radius).put(5.5);
		
		PVUnion pvu1 = PVDataFactory.getPVDataCreate().createPVVariantUnion();
		pvu1.set(spiralGeneratorPVStructure);
		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvu1;
		blockPVStructure.getUnionArrayField("generator.value.generators").put(0, unionArray.length, unionArray, 0);
        
        DummyMalcolmRecord pvRecord = new DummyMalcolmRecord(recordName, blockPVStructure);
        PVDatabase master = PVDatabaseFactory.getMaster();
        master.addRecord(pvRecord);
        return pvRecord;
    }

    public DummyMalcolmRecord(String recordName, PVStructure blockPVStructure) {
        super(recordName, blockPVStructure);
                    
        // process
        process();
    }

    public Service getService(PVStructure pvRequest)
    {
    	String methodName = pvRequest.getStringField("method").get();
        return new RPCServiceAsyncImpl(this, methodName);
    }
}
