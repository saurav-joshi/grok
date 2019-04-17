variable user {}
variable password {}
variable domain {}
variable endpoint {}
variable ssh_public_key_file {}
variable ssh_private_key_file {}

variable owner {}

variable compute_instance {
  description = "OARDC-Iaasimov"
  default = "OARDC-Iaasimov"
}

provider "opc" {
  user = "${var.user}"
  password = "${var.password}"
  identity_domain = "${var.domain}"
  endpoint = "${var.endpoint}"
}

resource "opc_compute_instance" "instance" {
  name = "${var.owner}-${var.compute_instance}-Server"
  label = "${var.owner} DevOps Day Server"
  shape = "oc3"
  image_list = "/oracle/public/OL_7.2_UEKR3_x86_64"
  ssh_keys = [ "${opc_compute_ssh_key.opc-server-sshkey.name}" ]
}

resource "opc_compute_ssh_key" "opc-server-sshkey" {
  name = "${var.owner}-${var.compute_instance}-sshkey"
  key = "${file(var.ssh_public_key_file)}"
  enabled = true
}

resource "opc_compute_ip_reservation" "opc-server-ipreservation" { 
  name = "${var.owner}-${var.compute_instance}-ip.reservation"
  parent_pool = "/oracle/public/ippool"
  permanent = true
}

resource "opc_compute_ip_association" "instance-ipreservation" {
  name = "${var.compute_instance}-ip"
  vcable = "${opc_compute_instance.instance.vcable}"
  parent_pool = "ipreservation:${opc_compute_ip_reservation.opc-server-ipreservation.name}"

  # After we asociate the IP to the instance, sleep 15 seconds, then to the remote-exec
  connection {
        host = "${opc_compute_ip_reservation.opc-server-ipreservation.ip}"
        type = "ssh"
	 user = "opc"
        timeout = "2m"
        agent = "false"
	private_key = "${file(var.ssh_private_key_file)}"
  }

  provisioner "local-exec" {
    command = "echo SERVER_IP=${opc_compute_ip_reservation.opc-server-ipreservation.ip} > server.properties"
  }

#  provisioner "local-exec" {
#    command = "wget https://bootstrap.cn.oracle.com/jenkins/job/Iaasimov/lastSuccessfulBuild/artifact/target/iaasimov-0.5.1-SNAPSHOT.jar"
#  }

  provisioner "remote-exec" {
    inline = [
      "sudo yum install java -y", 
      "sudo mkdir -p /iaasimov",
      "sudo chown opc:opc -R /iaasimov",
    ]
  }

  provisioner "file" {
    source = "iaasimov-0.5.1-SNAPSHOT.jar"
    destination = "/iaasimov/iaasimov-0.5.1-SNAPSHOT.jar"
  }

  provisioner "remote-exec" {
    inline = [
      "cd /iaasimov", 
      "nohup java -jar iaasimov-0.5.1-SNAPSHOT.jar -Xms2048M -Xmx4096M > /iaasimov/iaasimov.log &"
    ]
  }
}


