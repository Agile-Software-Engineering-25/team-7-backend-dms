#!/bin/bash

# Wait for MinIO to be ready
echo "Waiting for MinIO to be ready..."
until mc alias set minio "${MINIO_URL:-http://minio:9000}" "${MINIO_ROOT_USER:-minioadmin}" "${MINIO_ROOT_PASSWORD:-minioadmin}"; do
        echo "MinIO not ready yet, waiting..."
        sleep 5
done

echo "MinIO is ready! Setting up users, policies, and buckets..."

# Create buckets for different microservices (with idempotency)
echo "Creating buckets..."
mc mb minio/ase-team-7 --ignore-existing
mc mb minio/shared-bucket --ignore-existing

# Function to create a policy from a file in the policies folder
create_policy() {
        local policy_name="$1"
        local policy_file="/policies/${policy_name}.json"
        if [ -f "$policy_file" ]; then
                echo "Creating policy: $policy_name from $policy_file"
                mc admin policy create minio "$policy_name" "$policy_file"
        else
                echo "Policy file $policy_file not found, skipping."
        fi
}

# Create all policies from the policies folder (ignoring .ignore files)
echo "Creating policies from /policies..."
for policy_path in /policies/*.json; do
        policy_file=$(basename "$policy_path")
        policy_name="${policy_file%.json}"
        create_policy "$policy_name"
done

# Function to create a user from JSON file and env variables
# Takes a JSON file with "username" and "policy" fields
# Maps the username as an env variable to get the actual username and password
# E.g. for username "file-service", it looks for env vars MINIO_FILE_SERVICE_USER and MINIO_FILE_SERVICE_PASSWORD
# Creates the user and attaches the policy
create_user_from_json() {
        local json_file="$1"
        local user_name=$(jq -r '.username' "$json_file")
        local policy_name=$(jq -r '.policy' "$json_file")
        # Map username to env variable for password, e.g. MINIO_FILE_SERVICE_USER -> MINIO_FILE_SERVICE_PASSWORD
        local env_user_var="MINIO_$(echo "$user_name" | tr 'a-z-' 'A-Z_')_USER"
        local env_password_var="MINIO_$(echo "$user_name" | tr 'a-z-' 'A-Z_')_PASSWORD"
        local user_password="test"
        echo "Creating user: $user_env_name"
        mc admin user add minio "$user_name" "$user_password"
        if [ -n "$policy_name" ] && [ "$policy_name" != "null" ]; then
                echo "Assigning policy $policy_name to user $user_name"
                mc admin policy attach minio "$policy_name" --user "$user_name"
        else
                echo "No policy specified for user $user__name, skipping policy assignment."
        fi
}

# Create users from JSON files in /users folder
echo "Creating users from /users JSON files..."
for user_json in /users/*.json; do
        if [ -f "$user_json" ]; then
                create_user_from_json "$user_json"
        fi
done

echo "Setup complete!"
echo "==========================="
echo "MinIO Console: http://localhost:9001"
echo "MinIO API: http://localhost:9000"
echo "==========================="
