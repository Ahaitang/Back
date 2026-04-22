# MinIO Migration Script
# Run on server to migrate existing MinIO data

# 1. Backup existing MinIO data
docker exec minio ls /data
docker run --rm -v minio_data:/data -v /tmp/minio-backup:/backup alpine tar czf /backup/minio-backup.tar.gz /data

# 2. Stop old MinIO container
docker stop minio
docker rm minio

# 3. The new MinIO will auto-start with docker-compose up
# Data volume: hospital-minio-data

# 4. Restore data to new volume (if needed)
# docker run --rm -v hospital-minio-data:/data -v /tmp/minio-backup:/backup alpine tar xzf /backup/minio-backup.tar.gz -C /