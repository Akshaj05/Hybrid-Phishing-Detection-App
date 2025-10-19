from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
import os

DB_URL = os.getenv("DATABASE_URL", "sqlite:///./backend.db")
engine = create_engine(DB_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()
