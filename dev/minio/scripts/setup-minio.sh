#!/bin/bash

# Wait for MinIO to be ready
echo "Waiting for MinIO to be ready..."
until mc alias set minio "${MINIO_URL:-http://minio:9000}" "${MINIO_ROOT_USER:-minioadmin}" "${MINIO_ROOT_PASSWORD:-minioadmin}"; do
        echo "MinIO not ready yet, waiting..."
        sleep 5
done

echo "MinIO is ready! Setting up users, policies, and buckets..."

# Create buckets
echo "Creating buckets..."
mc mb minio/test --ignore-existing
mc mb minio/test-shared-bucket --ignore-existing

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

# Create users from JSON files in /users folder
echo "Creating test user"
mc admin user add minio "test" "testpassword"
mc admin policy attach minio test --user "test"

# Verbose logs
echo "Enabling verbose logging for MinIO..."
mc admin trace -v minio

echo "Setup complete!"
echo "==========================="
echo "MinIO Console: http://localhost:9001"
echo "MinIO API: http://localhost:9000"
echo "==========================="
