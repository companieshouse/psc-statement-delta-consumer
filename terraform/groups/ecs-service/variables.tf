# ------------------------------------------------------------------------------
# Environment
# ------------------------------------------------------------------------------
variable "environment" {
  type        = string
  description = "The environment name, defined in envrionments vars."
}
variable "aws_region" {
  default     = "eu-west-2"
  type        = string
  description = "The AWS region for deployment."
}
variable "aws_profile" {
  default     = "development-eu-west-2"
  type        = string
  description = "The AWS profile to use for deployment."
}

# ------------------------------------------------------------------------------
# Docker Container
# ------------------------------------------------------------------------------
variable "docker_registry" {
  type        = string
  description = "The FQDN of the Docker registry."
}

# ------------------------------------------------------------------------------
# Service performance and scaling configs
# ------------------------------------------------------------------------------
variable "desired_task_count" {
  type = number
  description = "The desired ECS task count for this service"
  default = 1 # defaulted low for dev environments, override for production
}
variable "required_cpus" {
  type = number
  description = "The required cpu resource for this service. 1024 here is 1 vCPU"
  default = 256 # defaulted low for dev environments, override for production
}
variable "required_memory" {
  type = number
  description = "The required memory for this service"
  default = 512 # defaulted low for node service in dev environments, override for production

}
variable "max_task_count" {
  type        = number
  description = "The maximum number of tasks for this service."
  default     = 3
}

variable "use_fargate" {
  type        = bool
  description = "If true, sets the required capabilities for all containers in the task definition to use FARGATE, false uses EC2"
  default     = true
}
variable "use_capacity_provider" {
  type        = bool
  description = "Whether to use a capacity provider instead of setting a launch type for the service"
  default     = true
}
variable "service_autoscale_enabled" {
  type        = bool
  description = "Whether to enable service autoscaling, including scheduled autoscaling"
  default     = true
}
variable "service_autoscale_target_value_cpu" {
  type        = number
  description = "Target CPU percentage for the ECS Service to autoscale on"
  default     = 50 # 100 disables autoscaling using CPU as a metric
}
variable "service_scaledown_schedule" {
  type        = string
  description = "The schedule to use when scaling down the number of tasks to zero."
  # Typically used to stop all tasks in a service to save resource costs overnight.
  # E.g. a value of '55 19 * * ? *' would be Mon-Sun 7:55pm.  An empty string indicates that no schedule should be created.

  default     = ""
}
variable "service_scaleup_schedule" {
  type        = string
  description = "The schedule to use when scaling up the number of tasks to their normal desired level."
  # Typically used to start all tasks in a service after it has been shutdown overnight.
  # E.g. a value of '5 6 * * ? *' would be Mon-Sun 6:05am.  An empty string indicates that no schedule should be created.

  default     = ""
}
variable "service_autoscale_scale_in_cooldown" {
  type        = number
  description = "Cooldown in seconds for ECS Service scale in (run fewer tasks)"
  default     = 300
}
variable "service_autoscale_scale_out_cooldown" {
  type        = number
  description = "Cooldown in seconds for ECS Service scale out (add more tasks)"
  default     = 300
}

# ----------------------------------------------------------------------
# Cloudwatch alerts
# ----------------------------------------------------------------------
variable "cloudwatch_alarms_enabled" {
  description = "Whether to create a standard set of cloudwatch alarms for the service.  Requires an SNS topic to have already been created for the stack."
  type        = bool
  default     = true
}

# ------------------------------------------------------------------------------
# Service environment variable configs
# ------------------------------------------------------------------------------

variable "ssm_version_prefix" {
  type        = string
  description = "String to use as a prefix to the names of the variables containing variables and secrets version."
  default     = "SSM_VERSION_"
}

variable "use_set_environment_files" {
  type        = bool
  default     = false
  description = "Toggle default global and shared environment files"
}

variable "psc_statement_delta_consumer_version" {
  type        = string
  description = "The version of the psc-statement-delta-consumer container to run."
}

variable "task_healthcheck_interval" {
  type        = number
  description = "Health check interval configuration for ECS task definitions."
  default     = 60
}
variable "task_healthcheck_timeout" {
  type        = number
  description = "Health check timeout configuration for ECS task definitions."
  default     = 5
}
variable "task_healthcheck_retries" {
  type        = number
  description = "Health check retries configuration for ECS task definitions."
  default     = 3
}

variable "task_healthcheck_start_period" {
  type        = number
  description = "Health check start period configuration for ECS task definitions."
  default     = 120
}