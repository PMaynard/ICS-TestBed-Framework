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
	mvn package -DskipTests # Skip tests.

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

## Prerequisites (Vagrant+VirtualBox)

Use the latest version of Vagrant over the pre-built/distribution packages as these scripts use features from the latest versions of Vagrant. *Should be fine if using Ubuntu 18.04.1 LTS*.

	git clone https://github.com/mitchellh/vagrant.git /opt/vagrant
	cd /opt/vagrant
	bundle install
	bundle --binstubs exec
	ln -sf /opt/vagrant/exec/vagrant /usr/local/bin/vagrant 

## [OPTIONAL] Create VM Image

This is an optional development step. It builds a virtual machine image, pre-configured to run the testbed nodes. If you don't want to create the latest version, the default option is get a stable image from Vagrant's image repository which stays in step with the master branch. 

[Packer](https://www.packer.io/) is used to create a virtual machine image suitable for VirtualBox and Vagrant:

	cd vagrant_image
	packer build vagrant-node.json
	vagrant box add testbed-node vagrant.box

## Deploy

**WARNING** You will need at least 4GB of free RAM.

Update the Vagrantfile with any additional information, such as static IP address and RAM usage.

The default IP settings are ```10.50.50.*```. ```.200``` is used for HMI and ```101-105``` for RTUs. The default RAM allocated per VM is 512MB. 

	vagrant up
	vagrant ssh hmi
	vagrant ssh rtu-1 # 1-5
	vagrant halt
	vagrant destroy 

# Example Dataset

An example dataset was created, using the default deployment configuration. PCAPs can be downloaded from [here](https://dx.doi.org/10.6084/m9.figshare.6133457.v1). The IEC104 MITM was performed using the ettercap plugin located [here](https://github.com/PMaynard/ettercap-104-mitm)

-   **\[Host-SCAN 13:45\]**: Basic network reconnaissance using a Nmap
    network wide scan. CMD: 'nmap -sn 10.50.50.\*'

-   **\[Host-SCAN 13:47\]**: Basic network reconnaissance looking for
    accessible [IEC104]{acronym-label="IEC104"
    acronym-form="singular+short"} servers. CMD: 'nmap 10.50.50.\* -p
    2404'

-   **\[Host-SCAN 13:47\]**: Full port scan of identified
    [RTUs]{acronym-label="RTU" acronym-form="plural+short"} nodes. CMD:
    'nmap 10.50.50.101-105 -A'

-   **\[Host-SCAN 13:49\]**: An active [IEC104]{acronym-label="IEC104"
    acronym-form="singular+short"} scan which probes the nodes using the
    [IEC104]{acronym-label="IEC104" acronym-form="singular+short"}
    protocol[^1]. CMD: 'nmap -Pn -n -d --script iec-identify.nse
    --script-args='iec-identify.timeout=500' -p 2404 10.50.50.101-105'

-   **\[Host-MITM 14:19\]**: Performs a [MITM]{acronym-label="MITM"
    acronym-form="singular+short"} on RTU-1 and the HMI. CMD: 'ettercap
    -i enp0s8 -T -M arp -P spoof\_104 /10.50.50.101/ /10.50.50.150/'

A numerical break down of the dataset is shown below:

|Host|IP|IEC104|OPC-UA|Other|**Total**
|-----------|--------------|--------|--------|--------|-----------|
|HMI|10.50.50.150|26,158|0|17,688|43,846|
|Historian|10.50.50.151|0|14,695|14,927|29,622|
|RTU-1|10.50.50.101|3,592|2,940|5,543|12,075|
|RTU-2|10.50.50.102|3,665|2,941|5,876|12,482|
|RTU-3|10.50.50.103|3,668|2,940|5,793|12,404|
|RTU-4|10.50.50.104|3,690|2,940|5,771|12,404|
|RTU-5|10.50.50.105|3,576|930|7,933|12,442|
|MITM|10.50.50.99|2,390|0|3,449|5,839|
|SCAN|10.50.50.3|15|0|28,351|28,366|

# Citation

Please cite this framework using the following format: 

	@conference{
	 author   = "Peter Maynard and Kieran McLaughlin and Sakir Sezer",
	 title 	  = "An Open Framework for Deploying Experimental SCADA Testbed Networks",
	 journal  = "5th International Symposium for ICS & SCADA Cyber Security Research",
	 year 	  = "2018"
	}

The dataset can be cited using DOI: [10.6084/m9.figshare.6133457.v1](https://dx.doi.org/10.6084/m9.figshare.6133457.v1)

The full paper can be found at: <https://petermaynard.co.uk/publication/an-open-framework-for-deploying-experimental-scada-testbed-networks/>
