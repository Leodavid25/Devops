output "resource_group_name" {
  value = azurerm_resource_group.platform.name
}

output "aks_name" {
  value = azurerm_kubernetes_cluster.aks.name
}

output "acr_login_server" {
  value = azurerm_container_registry.acr.login_server
}

output "acr_name" {
  value = azurerm_container_registry.acr.name
}

output "key_vault_name" {
  value = azurerm_key_vault.kv.name
}
