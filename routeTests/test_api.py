import os
import time
import requests


# Make sure the server is running before executing this script.
# You can override the base URL with:
#   BASE_URL=http://127.0.0.1:8080 python routeTests/test_api.py


BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080").rstrip("/")


def _login(email: str, password: str) -> str:
    r = requests.post(f"{BASE_URL}/auth/login", json={"email": email, "password": password}, timeout=10)
    assert r.status_code == 200, f"Login failed: HTTP {r.status_code} — {r.text}"
    token = r.json().get("accessToken")
    assert token, f"Missing accessToken in response: {r.text}"
    return token


def _auth_headers(token: str) -> dict:
    return {"Authorization": f"Bearer {token}"}


def test_admin_smoke() -> None:
    print("=== Admin: login + smoke endpoints ===")
    token = _login("admin1@school.com", "admin123")
    headers = _auth_headers(token)

    r = requests.get(f"{BASE_URL}/crud/admins/count", headers=headers, timeout=10)
    assert r.status_code == 200, f"GET /crud/admins/count failed: HTTP {r.status_code} — {r.text}"
    assert "count" in r.json(), f"Unexpected response for count: {r.text}"

    r = requests.get(f"{BASE_URL}/crud/admins", headers=headers, timeout=10)
    assert r.status_code == 200, f"GET /crud/admins failed: HTTP {r.status_code} — {r.text}"
    assert isinstance(r.json(), list), f"Expected list, got: {r.text}"

    print("OK\n")


def test_student_calendar_notes_roundtrip() -> None:
    print("=== Student: calendar-notes POST/GET upsert ===")
    token = _login("alice@student.school.com", "pass123")
    headers = _auth_headers(token)

    today = time.strftime("%Y-%m-%d")
    course_code = "INFO1"
    course_name = "Introduction to Programming"

    # 1) Create note
    payload = {
        "date": today,
        "courseCode": course_code,
        "courseName": course_name,
        "note": f"Test note (v1) @ {int(time.time())}",
    }
    r = requests.post(f"{BASE_URL}/crud/calendar_notes/me", json=payload, headers=headers, timeout=10)
    assert r.status_code == 200, f"POST /crud/calendar-notes/me failed: HTTP {r.status_code} — {r.text}"
    created = r.json()
    assert created["date"] == today
    assert created["courseCode"] == course_code
    assert created.get("note"), "Expected a note in response"

    # 2) Fetch notes for this date
    r = requests.get(
        f"{BASE_URL}/crud/calendar_notes/me",
        params={"startDate": today, "endDate": today},
        headers=headers,
        timeout=10,
    )
    assert r.status_code == 200, f"GET /crud/calendar-notes/me failed: HTTP {r.status_code} — {r.text}"
    notes = r.json()
    assert isinstance(notes, list), f"Expected list, got: {r.text}"
    assert any(n["date"] == today and n["courseCode"] == course_code for n in notes), "Created note not found in list"

    # 3) Upsert same (date, course) with new text
    payload2 = {
        "date": today,
        "courseCode": course_code,
        "courseName": course_name,
        "note": f"Test note (v2) @ {int(time.time())}",
    }
    r = requests.post(f"{BASE_URL}/crud/calendar_notes/me", json=payload2, headers=headers, timeout=10)
    assert r.status_code == 200, f"POST(upsert) /crud/calendar-notes/me failed: HTTP {r.status_code} — {r.text}"
    updated = r.json()
    assert updated["date"] == today
    assert updated["courseCode"] == course_code
    assert updated["note"] == payload2["note"], "Upsert did not update note"

    # 4) Fetch again and ensure only one entry for that course+date and it matches v2
    r = requests.get(
        f"{BASE_URL}/crud/calendar_notes/me",
        params={"startDate": today, "endDate": today},
        headers=headers,
        timeout=10,
    )
    assert r.status_code == 200, f"GET(after upsert) /crud/calendar-notes/me failed: HTTP {r.status_code} — {r.text}"
    notes2 = [n for n in r.json() if n["date"] == today and n["courseCode"] == course_code]
    assert len(notes2) == 1, f"Expected 1 note for ({today}, {course_code}), got {len(notes2)}"
    assert notes2[0]["note"] == payload2["note"], "Stored note does not match upserted note"

    print("OK\n")


def main() -> None:
    test_admin_smoke()
    test_student_calendar_notes_roundtrip()
    print("All route tests passed")


if __name__ == "__main__":
    main()