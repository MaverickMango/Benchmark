package root.entities.otherdataset;

import com.google.gson.annotations.SerializedName;

public class Strategy {
    @SerializedName("matrix")
    private Matrix matrix;

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }
}
