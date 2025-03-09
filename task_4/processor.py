import sqlite3
import csv
import os
from collections import defaultdict, Counter
from typing import List, Tuple, Any, Optional
import traceback

class ShipmentDatabase:
    def __init__(self, db_name: str = "shipment_database.db"):
        """
        Initialize the database connection and cursor.
        """
        self.db_name = db_name
        self.conn: Optional[sqlite3.Connection] = None
        self.cursor: Optional[sqlite3.Cursor] = None
        self.shipment_mapper: defaultdict[str, List[str]] = defaultdict(list)
    
    def get_db_connection(self) -> sqlite3.Connection:
        """
        Establishes a database connection if not already connected.
        """
        if self.conn is None:
            self.conn = sqlite3.connect(self.db_name)
        return self.conn

    def get_cursor(self) -> sqlite3.Cursor:
        """
        Returns a database cursor, creating one if needed.
        """
        if self.cursor is None or self.cursor.connection is None:
            self.cursor = self.get_db_connection().cursor()
        return self.cursor

    def execute_write_query(self, query: str, values: Tuple[Any, ...]) -> None:
        """
        Executes write queries (insert, update, delete) and commits changes.
        """
        try:
            cursor = self.get_cursor()
            cursor.execute(query, values)
            self.get_db_connection().commit()
        except sqlite3.Error as e:
            print(f"An error occurred executing write query: {str(e)}")

    def truncate_data(self) -> None:
        """
        Clears data from the 'product' and 'shipment' tables before new processing.
        """
        tables = ['product', 'shipment']
        for table in tables:
            query = f'DELETE FROM {table}'
            self.execute_write_query(query, ())
    
    def shipment_data_1(self, row: List[str]) -> None:
        """
        Processes product data and inserts into 'product' table.
        """
        name = row[1]
        self.shipment_mapper[row[0]].append(row[1])  # Track shipment and corresponding products
        try:
            insert_query = 'INSERT OR IGNORE INTO product (name) VALUES (?)'
            self.execute_write_query(insert_query, (name,))  
        except sqlite3.IntegrityError:
            print(f"Error: The name '{name}' already exists in the database.")

    def shipment_data_2(self, row: List[str]) -> None:
        """
        Processes shipment data and inserts into 'shipment' table.
        """
        shipment_identifier = row[0]
        products = Counter(self.shipment_mapper[shipment_identifier])  # Get product counts for this shipment
        
        for product_name, quantity in products.items():     
            query = "SELECT id FROM product WHERE name = ?"
            result = self.get_cursor().execute(query, (product_name,)).fetchone()
            if result:
                product_id = result[0]
                insert_query = 'INSERT INTO shipment (product_id, quantity, origin, destination) VALUES (?, ?, ?, ?)'
                self.execute_write_query(insert_query, (product_id, quantity, row[1], row[2]))
            else:
                print(f"Product '{product_name}' not found in the database.")
    
    def shipment_database_processor(self, file_path: str, file_number: int) -> None:
        """
        Reads CSV files dynamically based on file_number and processes them accordingly.
        """
        try:
            data_file = os.path.join(file_path, f'shipping_data_{file_number}.csv')
            if not os.path.exists(data_file):
                raise FileNotFoundError(f"The provided file {data_file} is not found in {file_path} directory")
            
            handler_function = getattr(self, f'shipment_data_{file_number}', None)
            if handler_function is None:
                raise ValueError(f"No handler function found for file number {file_number}")
            
            self.insert_csv_into_db(data_file, handler_function)
            print(f'{'='*5} {data_file} {'='*5} dump complete')
        except Exception as e:
            print(traceback.format_exc())
    
    def insert_csv_into_db(self, data_file: str, handler: Any) -> None:
        """
        Reads CSV file and processes rows with the appropriate handler function.
        """
        try: 
            print(f'{'=' * 5} Writing csv file {data_file} to shipment_database.db via {handler.__name__} function {'=' * 5}')
            with open(data_file, 'r') as shipping:
                reader = csv.reader(shipping) 
                next(reader)  # Skip header
                for row in reader:
                    handler(row)
        except Exception as e:
            raise RuntimeError(f'Error occurred while processing the CSV file: {e}')

    def close_connection(self) -> None:
        """
        Closes database cursor and connection if open.
        """
        if self.cursor:
            self.cursor.close()
        if self.conn:
            self.conn.close()

if __name__ == '__main__':
    # Initialize the database processor
    shipment_db = ShipmentDatabase()
    
    # Fetch the path for data directory
    main_path = os.getcwd()
    data_path = os.path.join(main_path, 'data')
    print(data_path)
    
    # Clean any existing data in tables before processing
    shipment_db.truncate_data()
    
    # Process shipment data dynamically
    shipment_db.shipment_database_processor(data_path, 1)
    shipment_db.shipment_database_processor(data_path, 2)
    
    # Close database connection
    shipment_db.close_connection()
