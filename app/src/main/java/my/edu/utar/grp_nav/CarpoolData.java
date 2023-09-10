package my.edu.utar.grp_nav;

public class CarpoolData {
    private String name, price, distance, date, phone, time, carModel, carColor, licensePlate;

    public CarpoolData(String name, String price, String distance, String date, String phone, String time, String carModel, String carColor, String licensePlate) {
        this.name = name;
        this.price = price;
        this.distance = distance;
        this.date = date;
        this.phone = phone;
        this.time = time;
        this.carModel = carModel;
        this.carColor = carColor;
        this.licensePlate = licensePlate;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDistance() {
        return distance;
    }

    public String getDate() {
        return date;
    }

    public String getPhone() {
        return phone;
    }

    public String getTime() {
        return time;
    }

    public String getCarModel() {
        return carModel;
    }

    public String getCarColor() {
        return carColor;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
}
