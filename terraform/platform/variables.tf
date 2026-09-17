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
  description = "VM size for the AKS system node pool."
  type        = string
  default     = "Standard_D2as_v7"
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

variable "pipeline_service_principal_object_id" {
  description = "Object ID of the Azure DevOps service connection's service principal (arm-devops). Grants it read access to Key Vault secrets so the deploy pipeline can fetch the JWT secret. Leave empty to skip."
  type        = string
  default     = ""
}
