package prototype.graph;

import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import prototype.core.Sandbox;
import prototype.vivant.Vivant;

public class Controls implements SimulatorView {
    private AnimationTimer timer;
        Slider speedSlider;

    @Override
    public void start() {
        Stage stage = new Stage();
        stage.setTitle("Simulation control panel");
        stage.setScene(new Scene(createContent()));
        stage.show();
    }

    private Parent createContent() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        Button pauseBtn = new Button("Pause");
        pauseBtn.setOnAction(evt -> timer.stop());
        Button continueBtn = new Button("Carry on");
        continueBtn.setOnAction(evt -> timer.start());
        HBox speedBox = new HBox();
        speedBox.setAlignment(Pos.CENTER);
        speedSlider = new Slider(0, 1, 0.5);  // in secs
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(0.25f);
        speedSlider.setBlockIncrement(0.1f);
        speedBox.getChildren().addAll(new Label("Slow"), speedSlider, new Label("Fast"));
        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(evt -> Platform.exit());
        root.getChildren().addAll(pauseBtn, continueBtn, quitBtn, speedBox);
        return root;
    }

    void setTimer(AnimationTimer timer) {
        this.timer = timer;
    }

    /**
     * Converts speed reading from secs to msecs.
     */
    long getSpeed() {
        return 1000 - (long) (1000 * speedSlider.getValue());
    }

    @Override
    public void setColor(Class<? extends Vivant> animalClass, Color color) {
    }

    @Override
    public void showStatus(int step, Sandbox field) {
    }

    @Override
    public boolean isViable(Sandbox field) {
        return true;
    }

    @Override
    public void reset() {
    }
}
