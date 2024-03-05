package examples.src;

public class TestClass {
    CarBand band;
    public TestClass(CarBand carBand) {
        this.band = carBand;
    }
    public void color() {
        for (int i = 0; i < band.toString().length(); i ++) {
            System.out.println("This car is ");
        }
        switch (band) {
            case DAZHONG:
                System.out.println("grey");
            case BMW:
                System.out.println("red");
                break;
            case AUDI:
            case BENZ:
                System.out.println("blue");
                break;
            default:
                System.out.println("black");
        }
    }
}
enum CarBand {
    BMW, AUDI, BENZ, DAZHONG,
}
