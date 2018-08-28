# ICS TestBed Framework 

A scalable framework for automatically deploying locally (or remotely) a number of virtual machines that replicate a Supervisory Control And Data Acquisition (SCADA) network is proposed. This includes multiple virtual hosts emulating sensors and actuators, with a Human Machine Interface (HMI) controlling the hosts.
The presented framework contains a collection of automation scripts which build and deploy a variable number of virtual machines, pre-configured to act as either a Remote Terminal Unit (RTU), HMI or Data Historian. The presented work includes a standards compliant implementation of IEC 60870-5-104 (IEC104) and OPC Unified Architecture (OPC-UA), with the capability to support other protocols such as Modbus-TCP (Modbus) and IEC61850. 

This allows researchers to build testbeds that can be configured to replicate real-world deployments of SCADA networks. The framework builds upon open source libraries and is released under the Free Software Foundation approved licence, GNU General Public License version 3.

# Example Operation

[![asciicast](https://asciinema.org/a/clpPltx8oGGQBxCKsz0FzIfqg.png)](https://asciinema.org/a/clpPltx8oGGQBxCKsz0FzIfqg)

# Potential Use Cases

- **Packet Generation**: The objective of the first use case is to generate network traffic from a large number of devices, with a high level of domain fidelity, where ideally process information would be included in the traffic. As highlighted earlier, creating datasets from a live system is not always possible: it is labour intensive, one needs to locate a suitable tap point and gain approval from the plant operators. This may be restricted by the plant operation and policies, as interference with a working network may lead to unforeseen circumstances and leak identifiable information. Packet generation can be used to test proposed changes to the SCADA network before being deployed into the live system. It can also perform stress testing of devices using legitimate looking packets. Interesting research use-cases include experimentation with different networking paradigms, such as ICN or IPv6, that have not been applied to ICS networks. 

- **Attack Simulations**: This case considers simulation of complex attacks on SCADA systems, and aims to perform  risk analysis of a replicated ICS network, without adversely affecting the live system. The testbed may be used by red teams in an attempt to compromise testbed nodes, whilst analysing the consequence and getting full packet capture analysis. If they are successful, the process can be re-performed with additional countermeasures in place, allowing testing of the new countermeasures in the context of security and how it may affect the site processes. The network captures of the read team exercise can be published as an open datasets for verifying IDS which can be reproduced and confirm the results by other researchers.  

- **Agent Benchmarking**: The objective of this use case is to support the benchmarking of agent host based systems, which are typically not performed on live systems due to vendor restrictions. Unless the agent is trusted by vendors, it is often prohibited from being deployed, with a risk of breach of contract. By using a testbed that accurately represents the real industrial site, it is possible to monitor the use of agent based software without causing disruptions. Provided the testbed is freely modifiable, functionally accurate and can integrate with physical hardware, it would be possible to perform a benchmark of the agent. 

- **Extending Limited Hardware**: The final case is to extend an existing physical testbed to include communication protocols and configurations which were not possible with the existing hardware. This could require the use of network emulators, to extend the networking equipment, and process simulators, to extend the process control equipment. By coupling virtual and physical hardware together, it is possible to create a complex and highly realistic testbed, allowing for large deployments which combine multiple protocols and devices to be created and analysed. This use case could be used alongside the others to enhance their results.

# Build and run locally

Clone the repository and install the required dependencies:

	git clone --recurse-submodules git@github.com:PMaynard/ICS-TestBed-Framework.git
 	sudo apt install openjdk-8-jdk maven

Build: 

	mvn clean package
	mvn package -Dmaven.test.skip=true # Skip tests.

Start up a RTU:

	java -jar node/target/node-1.0.jar
	shell> rtu
	shell> rtu-iec104port 2404
	shell> rtu-listen 127.0.0.1
	shell> run

Start up a HMI: 

	java -jar node/target/node-1.0.jar
	shell> hmi
	shell> hmi-interval 1000
	shell> remote-hosts 127.0.0.1
	shell> run

# Auto Deploy in VMs

The default configuration profile will deploy 1 HMI and 4 RTUs. The HMI will integrate the RTUs using the IEC104 and OPC-UA. The RTUs are configured to return random process data. 

## Prerequisites 

Use the latest version of Vagrant over the pre-built/distribution packages as these scripts use features from the latest versions of Vagrant. *Should be fine if using Ubuntu 18.04.1 LTS*.

	git clone https://github.com/mitchellh/vagrant.git /opt/vagrant
	cd /opt/vagrant
	bundle install
	bundle --binstubs exec
	ln -sf /opt/vagrant/exec/vagrant /usr/local/bin/vagrant 

## Deploy
	
	cp Vagrantfile.default Vagrantfile
	vagrant up

