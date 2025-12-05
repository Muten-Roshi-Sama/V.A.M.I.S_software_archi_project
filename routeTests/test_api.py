
# test_api.py
import requests
import json

# Make sure the server is running before executing these tests

#    Response :
    #        === Testing Login ===
    #        Status: 200
    #        Response: {"accessToken":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJlY2FtIiwiYXVkIjoiZWNhbS1hdWRpZW5jZSIsImlkIjoxLCJyb2xlIjoiYWRtaW4iLCJleHAiOjE3NjQ5NDYxMzh9.WWeUdNvFIXthmE22Az9jXwgMeX83yv4S7-icubm56og"}
    #        Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJlY2FtIiwiYXVkIjoiZWNhbS1hdWRpZW5jZSIsImlkIjoxLCJyb2xlIjoiYWRtaW4iLCJleHAiOjE3NjQ5NDYxMzh9.WWeUdNvFIXthmE22Az9jXwgMeX83yv4S7-icubm56og
    #
    #    === Testing GET all admins ===
    #    Status: 200
    #    Response: [{"id":1,"firstName":"admin134565432345676543","lastName":"One","email":"admin1@admin.com","createdAt":"2025-12-05T13:36:36.943448200Z"},{"id":2,"firstName":"admin234566543","lastName":"Two","email":"admin2@admin.com","createdAt":"2025-12-05T13:36:37.047480100Z"}]
    #
    #    === Testing GET admin by ID (1) ===
    #    Status: 200
    #    Response: {"id":1,"firstName":"admin134565432345676543","lastName":"One","email":"admin1@admin.com","createdAt":"2025-12-05T13:36:36.943448200Z"}
    #
    #    === Testing GET admin count ===
    #    Status: 200
    #    Response: {"count":2}
    #
    #    === Testing CREATE admin ===
    #    Status: 201
    #    Response: {"id":3,"firstName":"Test","lastName":"Admin","email":"test@admin.com","createdAt":"2025-12-05T13:48:59.312270800Z"}
#




BASE_URL = "http://localhost:8080"

# 1. Test login
print("=== Testing Login ===")
login_data = {
    "email": "admin1@admin.com",
    "password": "pass123"
}
response = requests.post(f"{BASE_URL}/auth/login", json=login_data)
print(f"Status: {response.status_code}")
print(f"Response: {response.text}")

if response.status_code == 200:
    token = response.json()["accessToken"]
    print(f"Token: {token}\n")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    # 2. Test GET all admins
    print("=== Testing GET all admins ===")
    response = requests.get(f"{BASE_URL}/crud/admins", headers=headers)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}\n")
    
    # 3. Test GET admin by ID
    print("=== Testing GET admin by ID (1) ===")
    response = requests.get(f"{BASE_URL}/crud/admins/by/1", headers=headers)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}\n")
    
    # 4. Test GET admin count
    print("=== Testing GET admin count ===")
    response = requests.get(f"{BASE_URL}/crud/admins/count", headers=headers)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}\n")
    
    # 5. Test CREATE admin
    print("=== Testing CREATE admin ===")
    new_admin = {
        "email": "test@admin.com",
        "firstName": "Test",
        "lastName": "Admin",
        "phoneNumber": "+32123456789",
        "password": "testpass"
    }
    response = requests.post(f"{BASE_URL}/crud/admins", json=new_admin, headers=headers)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}\n")
else:
    print("Login failed!")