variable "project" {
  description = "Short name used as a prefix for every resource."
  type        = string
  default     = "devops"
}

variable "location" {
  description = "Azure region for the platform resources."
  type        = string
  default     = "eastus"
}

variable "node_vm_size" {
  description = "VM size for the AKS system node pool. B-series is burstable and cheap for a demo workload."
  type        = string
  default     = "Standard_B2s"
}

variable "node_count_min" {
  description = "Minimum node count for the AKS autoscaler."
  type        = number
  default     = 1
}

variable "node_count_max" {
  description = "Maximum node count for the AKS autoscaler."
  type        = number
  default     = 3
}
