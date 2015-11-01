package se.N540;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Scenario1 {

	private static final Integer NUMBER_OF_VM = 300;
	private static final Integer NUMBER_OF_TASKS = 500;
	private static final Integer NUMBER_OF_HOSTS = 50;
	
	public static void main(String[] args) {
		Log.printLine("Starting OneDC...");

		try {
			// Number of cloud users
			Integer num_users = 1;
			// Current date
			Calendar cal = Calendar.getInstance();
			// Trace events
			boolean trace_flag = false;
			CloudSim.init(num_users, cal, trace_flag);

			// Create DC
			Datacenter dc0 = createDatacenter("Datacenter_0");

			// Create broker
			DatacenterBroker broker = createBroker();
			Integer brokerId = broker.getId();

			// Create VMs
			List<Vm> vmList = createVMs(NUMBER_OF_VM, brokerId);

			broker.submitVmList(vmList);

			// Create Cloudlet
			List<Cloudlet> taskList = createTasks(NUMBER_OF_TASKS, brokerId);

			broker.submitCloudletList(taskList);

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Pring result
			List<Cloudlet> results = broker.getCloudletReceivedList();
			printCloudletList(cal, results);
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.printLine("Something went wrong!");
		}
	}

	// Create static tasks
	private static List<Cloudlet> createTasks(Integer numberOfTasks,
			Integer brokerId) {
		List<Cloudlet> taskList = new ArrayList<>();

		// milliseconds
		Integer taskDuration = 60000;
		Integer numberOfCpu = 2;
		Integer taskFilesize = 800000;
		Integer taskOutputSize = 300;
		//UtilizationModel utilModel = new UtilizationModelStochastic();
		UtilizationModel utilModel = new UtilizationModelStochastic();

		for (Integer i = 0; i < numberOfTasks; i++) {
			Cloudlet tmpTask = new Cloudlet(i, taskDuration, numberOfCpu,
					taskFilesize, taskOutputSize, utilModel, utilModel,
					utilModel);
			tmpTask.setUserId(brokerId);
			// tmpTask.setVmId(vmId);
			taskList.add(tmpTask);
		}

		return taskList;
	}

	// Create VMs
	private static List<Vm> createVMs(Integer numberOfVms, Integer brokerId) {
		List<Vm> vmList = new ArrayList<>();

		Integer MIPS = 1000;
		Integer cpuNumber = 2;
		Integer ram = 4000000;
		Integer bw = 1000;
		Integer imageSize = 160000;
		String vmm = "Xen";

		for (Integer i = 0; i < numberOfVms; i++) {
			Vm tmpVM = new Vm(i, brokerId, MIPS, cpuNumber, ram, bw, imageSize,
					vmm, new CloudletSchedulerTimeShared());

			vmList.add(tmpVM);
		}

		return vmList;
	}

	// Create CPU for hosts
	private static List<Pe> createCPUs(Integer numberOfCpu) {
		List<Pe> cpuList = new ArrayList<>();
		Integer MIPS = 1000;

		for (Integer i = 0; i < numberOfCpu; i++) {
			Pe tmpPe = new Pe(i, new PeProvisionerSimple(MIPS));
			cpuList.add(tmpPe);
		}

		return cpuList;
	}

	private static List<Host> createHosts(Integer numberOfHosts,
			Integer availableRam, Integer availableBw, long storage,
			List<Pe> cpuList) {
		List<Host> hostList = new ArrayList<>();

		for (Integer i = 0; i < numberOfHosts; i++) {
			Host tmpHost = new Host(i, new RamProvisionerSimple(availableRam),
					new BwProvisionerSimple(availableBw), storage, cpuList,
					new VmSchedulerTimeShared(cpuList));

			hostList.add(tmpHost);
		}

		return hostList;
	}

	private static Datacenter createDatacenter(String name) {
		// PE (Processing Elements) CPU
		List<Pe> peList = createCPUs(11);

		// Host specs
		Integer RAM = 32000000;
		long storage = 64000000;
		Integer bw = 10000;

		// List to store the machines
		List<Host> hostList = createHosts(NUMBER_OF_HOSTS, RAM, bw, storage, peList);

		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		// Timezone of the resource
		double time_zone = 1.0;
		// Cost of using that resource
		double PEcost = 3.0;
		double memCost = 0.5;
		double storageCost = 0.05;
		double bwCost = 0.01;

		// Storage
		List<Storage> storageList = new ArrayList<Storage>();

		DatacenterCharacteristics dcSpec = new DatacenterCharacteristics(arch,
				os, vmm, hostList, time_zone, PEcost, memCost, storageCost,
				bwCost);

		// Finally create the DC
		Datacenter dc = null;
		try {
			dc = new Datacenter(name, dcSpec, new VmAllocationPolicySimple(
					hostList), storageList, 0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return dc;
	}

	// Broker to submit cloudlets
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		return broker;
	}

	private static String printArray(String[] array) {
		StringBuilder sb = new StringBuilder();
		for (Integer i = 0; i < array.length; i++) {
			sb.append(array[i]).append(" ");
		}

		return sb.toString();
	}

	private static void printCloudletList(Calendar cal, List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
		StringBuilder sb = new StringBuilder();
		BufferedWriter bout = createOutput(cal);
		DecimalFormat dft = new DecimalFormat("###.##");

		String indent = "    ";
		try {
			bout.write("========== OUTPUT ============\n");
			sb.append("Cloudlet ID").append(indent).append("STATUS").append(indent)
				.append("Data center ID").append(indent).append("VM ID").append(indent)
				.append("Time").append(indent).append("Start time").append(indent)
				.append("Finish time").append(indent).append("Resources").append("\n");
			
			for (Integer i = 0; i < size; i++) {
				cloudlet = list.get(i);
				sb.append(indent).append(cloudlet.getCloudletId()).append(indent).append(indent);
				sb.append(cloudlet.getCloudletStatusString());
				sb.append(indent).append(indent).append(cloudlet.getResourceId())
					.append(indent).append(indent).append(indent).append(cloudlet.getVmId())
					.append(indent).append(indent).append(dft.format(cloudlet.getActualCPUTime()))
					.append(indent).append(indent).append(dft.format(cloudlet.getExecStartTime()))
					.append(indent).append(indent).append(dft.format(cloudlet.getFinishTime()))
					.append(indent).append(indent).append(printArray(cloudlet.getAllResourceName()))
					.append("\n");
			}
			
			sb.append("Cloudlet ID").append(indent).append("STATUS").append(indent)
			.append("Data center ID").append(indent).append("VM ID").append(indent)
			.append("Time").append(indent).append("Start time").append(indent)
			.append("Finish time").append(indent).append("Resources").append("\n");
			
			bout.write(sb.toString());
			bout.flush();
			bout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time" + indent
				+ "Resources");

		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);
			
			// if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
			Log.print(cloudlet.getCloudletStatusString());

			Log.printLine(indent + indent + cloudlet.getResourceId() + indent
					+ indent + indent + cloudlet.getVmId() + indent + indent
					+ dft.format(cloudlet.getActualCPUTime()) + indent + indent
					+ dft.format(cloudlet.getExecStartTime()) + indent + indent
					+ dft.format(cloudlet.getFinishTime()) + indent + indent
					+ printArray(cloudlet.getAllResourceName()));
		}
		// }

		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time" + indent
				+ "Resources");
	}
	
	private static BufferedWriter createOutput(Calendar cal) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm");
		String outputFile = Paths.get("results", "scenario1_3_" + sdf.format(cal.getTime())).toString();
		File output = new File(outputFile);
		BufferedWriter bout = null;
		
		try {
			bout = new BufferedWriter(new FileWriter(output));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return bout;
	}
}
