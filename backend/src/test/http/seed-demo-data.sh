#!/bin/zsh
set -e

token=$(curl -s -X POST http://localhost:8180/realms/air-freight/protocol/openid-connect/token \
  -d "grant_type=password&client_id=air-freight-api&username=t_admin&password=t_password&scope=openid" \
  | python3 -c 'import json, sys; print(json.load(sys.stdin)["access_token"])')

read -r schedules bookings <<< "$(python3 - <<'EOF'
import json
from datetime import datetime, timedelta, timezone

def flight(number, departure, arrival_airport="ATH"):
    arrival = departure + timedelta(hours=3, minutes=30)
    return {
        "flightNumber": number, "departureAirport": "BRU", "arrivalAirport": arrival_airport,
        "aircraftRegistration": "SXBAA", "firstDate": departure.date().isoformat(),
        "lastDate": departure.date().isoformat(), "weekdays": [departure.strftime("%A").upper()],
        "departureTime": departure.strftime("%H:%M"), "arrivalTime": arrival.strftime("%H:%M"),
    }

def uld(number, departure, code, weight, destination, unit="KG"):
    return {
        "flightNumber": number, "scheduledDepartureDate": departure.date().isoformat(),
        "departureAirport": "BRU", "uldCode": code, "weight": weight, "weightUnit": unit,
        "destination": destination,
    }

now = datetime.now(timezone.utc).replace(second=0, microsecond=0)
now -= timedelta(minutes=now.minute % 5)
soon = now + timedelta(hours=3)
later = now + timedelta(hours=5)
tomorrow = now + timedelta(days=1)

schedules = [flight("HJ621", soon), flight("HJ705", later, "MAD"), flight("HJ810", tomorrow, "FRA")]
bookings = [
    uld("HJ621", soon, "AKE16689R7", 1000, "ATH"),
    uld("HJ621", soon, "AKE16690R7", 1050, "ATH"),
    uld("HJ621", soon, "PMC20001R7", 2400, "ATH"),
    uld("HJ621", soon, "PMC20002R7", 2500, "ATH"),
    uld("HJ621", soon, "AKE16691R7", 900, "SKG"),
    uld("HJ621", soon, "AKE16692R7", 2200, "ATH", "LB"),
    uld("HJ705", later, "AKE30001R7", 1200, "MAD"),
    uld("HJ705", later, "AKE30002R7", 1300, "MAD"),
    uld("HJ810", tomorrow, "PMC41001R7", 2000, "FRA"),
]
print(json.dumps(schedules, separators=(",", ":")), json.dumps(bookings, separators=(",", ":")))
EOF
)"

curl -s -f -o /dev/null -X POST http://localhost:8080/api/simulator/schedules \
  -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d "$schedules"
sleep 3
curl -s -f -o /dev/null -X POST http://localhost:8080/api/simulator/bookings \
  -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d "$bookings"

echo "Demo flights are ready: HJ621 and HJ705 have an open loading window, HJ810 opens later."
