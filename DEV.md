# Development Notes

## Creating Vagrant boxes. 

Requirements for vagrant box (performed by the Ansible role): 
- vagrant user
- password vagrant
- added vagrant ssh key
- password-less sudo access

Create a box by running:

	vagrant package --base <VBox VM name> 
	vagrant box add testbed-node package.box

Remove: 

	vagrant box remove testbed-node
