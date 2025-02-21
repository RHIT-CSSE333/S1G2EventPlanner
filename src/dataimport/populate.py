import pyodbc
import json
import random
import string
import hashlib
from datetime import datetime, timedelta


def generate_random_id(length=50):
    return ''.join(random.choices(string.ascii_letters + string.digits + '_.-', k=length))


def insert_person(cursor, person_data):
    # Convert phone number format from +1-555-123-4567 to 5551234567
    phone = ''.join(filter(str.isdigit, person_data['phone_number']))[1:]
    
    password_hash, password_salt = "xwCxcDhda2Gx2XMqYb4+kw==", "AjAsdeFZwD03te+CxxMSAw=="   # Hash and salt for 12345
    
    sql = """
    INSERT INTO Person (Email, PhoneNo, FirstName, MInit, LastName, DOB, PasswordHash, PasswordSalt)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """
    cursor.execute(sql, (
        person_data['email'],
        phone,
        person_data['first_name'],
        person_data['middle_initial'],
        person_data['last_name'],
        person_data['date_of_birth'],
        password_hash,
        password_salt
    ))
    return cursor.execute("SELECT @@IDENTITY").fetchval()


def insert_venue(cursor, venue_data):
    pricing_type = 1 if venue_data['pricing_type'] == 'Per day' else 0
    
    sql = """
    INSERT INTO Venue (Name, MaxCapacity, PricingType, Price, State, City, StreetAddress, ZipCode)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """
    cursor.execute(sql, (
        venue_data['name'],
        venue_data['max_capacity'],
        pricing_type,
        venue_data['price'],
        venue_data['state'],
        venue_data['city'],
        venue_data['street_address'],
        int(venue_data['zip_code'])
    ))
    return cursor.execute("SELECT @@IDENTITY").fetchval()


def insert_vendor(cursor, vendor_data):
    sql = "INSERT INTO Vendor (Name) VALUES (?)"
    cursor.execute(sql, (vendor_data['name'],))
    return cursor.execute("SELECT @@IDENTITY").fetchval()


def insert_service(cursor, vendor_id, service_data):
    sql = """
    INSERT INTO Service (Name, Description, Price, VendorID)
    VALUES (?, ?, ?, ?)
    """
    cursor.execute(sql, (
        service_data['name'],
        service_data['description'],
        service_data['price'],
        vendor_id
    ))
    return cursor.execute("SELECT @@IDENTITY").fetchval()


def insert_event(cursor, event_data, venue_id):
    registration_deadline = datetime.strptime(event_data['start_time'], '%Y-%m-%d %H:%M:%S') - timedelta(days=1)
    
    sql = """
    INSERT INTO Event (Name, StartTime, EndTime, VenueID, isPublic, Price, 
                      PaymentStatus, PaymentId, CheckInId, RegistrationDeadline)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """
    cursor.execute(sql, (
        event_data['name'],
        event_data['start_time'],
        event_data['end_time'],
        venue_id,
        1,  # isPublic
        0,  # Price
        0,  # PaymentStatus
        generate_random_id(),
        generate_random_id(),
        registration_deadline
    ))
    return cursor.execute("SELECT @@IDENTITY").fetchval()


def main():
    with open('old.json', 'r') as f:
        data = json.load(f)
    
    conn = pyodbc.connect('DRIVER={SQL Server};SERVER=golem.csse.rose-hulman.edu;DATABASE=EventPlannerS1G2_DEMO;Trusted_Connection=no;UID=anisima;PWD=databasepassword;')
    cursor = conn.cursor()
    
    try:
        person_ids = {}
        venue_ids = {}
        vendor_ids = {}
        service_ids = {}
        events_to_process = []
        
        for venue in data['Venues']:
            venue_ids[venue['name']] = insert_venue(cursor, venue)
        
        for vendor in data['Vendors']:
            vendor_id = insert_vendor(cursor, vendor)
            vendor_ids[vendor['name']] = vendor_id
            
            for service in vendor['services_provided']:
                service_ids[f"{vendor['name']}_{service['name']}"] = insert_service(cursor, vendor_id, service)
        
        # Insert all People
        for person in data['People']:
            person_id = insert_person(cursor, person)
            person_ids[person['unique_username']] = person_id
            
            if 'events_hosted' in person:
                for event in person['events_hosted']:
                    events_to_process.append({
                        'host_id': person_id,
                        'event_data': event
                    })
        
        # Process all Events and their relationships
        for event_info in events_to_process:
            event_data = event_info['event_data']
            host_id = event_info['host_id']
            venue_id = venue_ids[event_data['venue']['name']]
            event_id = insert_event(cursor, event_data, venue_id)
            
            cursor.execute("""
                INSERT INTO HostsEvent (PersonID, EventID)
                VALUES (?, ?)
            """, (host_id, event_id))
            
            if 'attendees' in event_data:
                for attendee in event_data['attendees']:
                    attendee_id = person_ids[attendee]
                    cursor.execute("""
                        INSERT INTO AttendsEvent (PersonID, EventID, PaymentStatus, PaymentId)
                        VALUES (?, ?, ?, ?)
                    """, (attendee_id, event_id, 0, generate_random_id()))
            
            if 'vendor_services' in event_data:
                for vendor_service in event_data['vendor_services']:
                    vendor_name = vendor_service['vendor']
                    for service_name in vendor_service['services']:
                        service_id = service_ids[f"{vendor_name}_{service_name}"]
                        cursor.execute("""
                            INSERT INTO EventService (EventID, ServiceID)
                            VALUES (?, ?)
                        """, (event_id, service_id))
        
        conn.commit()
        print("Database populated successfully!")
        
    except Exception as e:
        conn.rollback()
        print(f"Error occurred: {e}")
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
