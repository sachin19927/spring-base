CREATE TABLE deliveries (
                            id UUID PRIMARY KEY,
                            vehicle_id VARCHAR(255) NOT NULL,
                            address VARCHAR(255) NOT NULL,
                            started_at TIMESTAMP WITH TIME ZONE NOT NULL,
                            status VARCHAR(32) NOT NULL
);

