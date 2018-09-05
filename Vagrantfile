# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box_check_update = true
  config.vm.provider "virtualbox" do |vb|
    vb.memory = 512 
  end  

  config.vm.provision "ansible_local" do |ansible|
    ansible.playbook = "playbook.yml"
    ansible.compatibility_mode = "2.0"
  end
  
  (1..5).each do |i|
    config.vm.define "rtu-#{i}" do |node|
      node.vm.hostname = "rtu"
      node.vm.box = "pmaynard/testbed-node"
      # node.vm.box = "testbed-node" # [OPTIONAL] If using a locally created image.
      node.vm.network "public_network", ip: "10.50.50.10#{i}"
    end
  end

  config.vm.define "hmi" do |hmi|
    hmi.vm.hostname = "hmi"
    hmi.vm.box = "pmaynard/testbed-node"
    # hmi.vm.box = "testbed-node" # [OPTIONAL] If using a locally created image.
    hmi.vm.network "public_network", ip: "10.50.50.200"
  end
end
