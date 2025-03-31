package org.example.pw_projekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.concurrent.Semaphore;


public class Main extends Application {

    //pojemności magazynów
    int MAGAZYN1_SIZ = 100;
    int MAGAZYN2_SIZ = 100;
    int MAGAZYN3_SIZ = 100;
    int[] iloscWMagazynie1 = {0};
    int[] iloscWMagazynie2 = {0};
    int[] iloscWMagazynie3 = {0};
    Rectangle[] recMag1 = new Rectangle[MAGAZYN1_SIZ];
    Rectangle[] recMag2 = new Rectangle[MAGAZYN2_SIZ];
    Rectangle[] recMag3 = new Rectangle[MAGAZYN3_SIZ];

    //Do ochrony magazynu 1
    Semaphore pelny1 = new Semaphore(0);
    Semaphore pusty1 = new Semaphore(MAGAZYN1_SIZ);
    Semaphore chronK1 = new Semaphore(1);
    Semaphore chronP1 = new Semaphore(1);

    //Do ochrony magazynu 2
    Semaphore pelny2 = new Semaphore(0);
    Semaphore pusty2 = new Semaphore(MAGAZYN2_SIZ);
    Semaphore chronK2 = new Semaphore(1);

    //Do ochrony magazynu 3
    Semaphore pelny3 = new Semaphore(0);
    Semaphore pusty3 = new Semaphore(MAGAZYN3_SIZ);
    Semaphore chronP3 = new Semaphore(1);
    private AnchorPane root;


    @Override
    public void start(Stage stage) throws IOException {

        //wybór liczby linii produkcyjnych
        ComboBox<Integer> comboBox1 = new ComboBox<>();
        comboBox1.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        comboBox1.setValue(8);

        //wybór liczby dostawczaków
        ComboBox<Integer> comboBox2 = new ComboBox<>();
        comboBox2.getItems().addAll(1, 2, 3, 4, 5, 6, 7);
        comboBox2.setValue(4);

        //wybór pojemnosci magazynów
        ComboBox<Integer> comboBox3 = new ComboBox<>();
        comboBox3.getItems().addAll(10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
        comboBox3.setValue(100);

        //wybór predkosci symulacji
        ComboBox<Integer> comboBox4 = new ComboBox<>();
        comboBox4.getItems().addAll(1, 3);
        comboBox4.setValue(1);

        //wybór ilości elementów wyjmowanych w porcji z magazynu 3
        TextField wielkoscPaczek = new TextField();
        wielkoscPaczek.setText("20");

        //wybór czasu podjazdu auta dostawczego
        TextField czasNaPodjazd = new TextField();
        czasNaPodjazd.setText("2");

        //etykiedy dla wyboru użytkownika
        Label ileLinii = new Label("   Wybierz liczbę\n   linii produkcyjnych");
        Label ileDostawczakow = new Label("   Wybierz liczbę\n   aut dostawczych");
        Label pojemnoscMag = new Label("  Podaj\n  pojemność magazynów");
        Label czasSym = new Label("  Symulacja\n  1 - szybciej\n  3 - wolniej");
        Label paczki = new Label("  Wybierz wielkość paczek [szt.]\n  wyjmowanych z magazynu 3");
        Label czasPodj = new Label("   Wybierz czas [s] podjazdu\n   auta dostawczego");

        //przycisk startujący
        Button start = new Button("START");

        root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));

        VBox vbox = new VBox(10); // 10 - odstęp między elementami
        vbox.setStyle("-fx-background-color: lightgray;");
        vbox.getChildren().addAll(ileLinii, comboBox1, ileDostawczakow, comboBox2, pojemnoscMag, comboBox3, czasSym, comboBox4, paczki, wielkoscPaczek, czasPodj, czasNaPodjazd, start);
        root.getChildren().add(vbox);
        AnchorPane.setTopAnchor(vbox, 5.0);    // ustawienie odległości od góry
        AnchorPane.setLeftAnchor(vbox, 5.0);   // ustawienie odległości od lewej strony

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Projekt: zakład produkcyjny");
        stage.show();

        //obrazek auta dostawczego
        Image auto = new Image("file:/obrazy/auto.jpg");
        ImagePattern imageAuto = new ImagePattern(auto);

        //Rectangle recP = new Rectangle();

        /*for (int i = 0; i < MAGAZYN1_SIZ; i++) {
            recMag1[i] = recP;
            recMag2[i] = recP;
            recMag3[i] = recP;
        }*/

        //start symulacji
        start.setOnAction(actionEvent -> {
            int ileLinii1 = comboBox1.getValue();
            int ileDostawczakow1 = comboBox2.getValue();
            int pojemnMagaz = comboBox3.getValue();
            int predkosc = comboBox4.getValue();
            int ileElemZMag3 = Integer.parseInt(wielkoscPaczek.getText());
            int czasPodjazdu = Integer.parseInt(czasNaPodjazd.getText());

            //ustawienie pojemności magazynów
            MAGAZYN1_SIZ = pojemnMagaz;
            MAGAZYN2_SIZ = pojemnMagaz;
            MAGAZYN3_SIZ = pojemnMagaz;

            utworzLinieNaEkranie(ileLinii1, root); //utworzenie linii na ekranie (prostokąty)
            utworzMagazyny(pojemnMagaz, root); //utworzenie magazynów o zadanej pojemnosci

            //tekst "Auta dostawcze" - ustawienie
            Text infoOAutach = new Text();
            infoOAutach.setX(800);
            infoOAutach.setY(10);
            infoOAutach.setText("Auta dostawcze");
            root.getChildren().add(infoOAutach);

            Thread[] LinieProd = new LiniaProdukcyjna[ileLinii1];
            Thread[] dostawczak = new Dostawczak[ileDostawczakow1];
            for (int i = 0; i < ileLinii1; i++) {
                LinieProd[i] = new LiniaProdukcyjna(predkosc, MAGAZYN1_SIZ, MAGAZYN2_SIZ, MAGAZYN3_SIZ, recMag3, root, 190 + (i * 50), 280, chronK1, pusty1, pelny1, chronK2, pusty2, pelny2, pusty3, pelny3, chronP3, iloscWMagazynie1, iloscWMagazynie2, iloscWMagazynie3, recMag1, recMag2);
            }
            for (int i = 0; i < ileDostawczakow1; i++) {
                dostawczak[i] = new Dostawczak(MAGAZYN1_SIZ, MAGAZYN2_SIZ, 780, 20 + (i * 90), czasPodjazdu, chronK1, chronK2, chronP1, pusty1, pelny1, pusty2, pelny2, imageAuto, root, iloscWMagazynie1, iloscWMagazynie2, recMag1, recMag2);
            }
            Thread odbiorPaczek = new OdbiorPaczek(MAGAZYN3_SIZ, ileElemZMag3, iloscWMagazynie3, root, recMag3, chronP3, pusty3, pelny3);

            //start wątków
            for (int i = 0; i < ileLinii1; i++) {
                LinieProd[i].start();
            }
            for (int i = 0; i < ileDostawczakow1; i++) {
                dostawczak[i].start();
            }
            odbiorPaczek.start();

            // Utworzenie nowego wątku do oczekiwania na zakończenie pracy pozostałych wątków
            new Thread(() -> {
                try {
                    for (int i = 0; i < ileLinii1; i++) {
                        LinieProd[i].join();
                    }
                    for (int i = 0; i < ileDostawczakow1; i++) {
                        dostawczak[i].join();
                    }
                    odbiorPaczek.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Platform.runLater(() -> System.out.println("Wszystkie wątki zakończyły pracę."));
            }).start();


        });

    }

    //funkcja rysująca linie produkcyjne na ekranie
    public void utworzLinieNaEkranie(int ileL, AnchorPane root) {
        for (int i = 0; i < ileL; i++) {
            Rectangle liniaProd;
            liniaProd = new Rectangle(190 + (i * 50), 280, 30, 100);
            liniaProd.setFill(Color.SKYBLUE);
            liniaProd.setStroke(Color.BLACK);
            liniaProd.setStrokeWidth(1);
            liniaProd.setArcHeight(8);
            liniaProd.setArcWidth(8);
            root.getChildren().add(liniaProd);
        }

        Text infoOLiniach = new Text();
        infoOLiniach.setX(190);
        infoOLiniach.setY(400);
        infoOLiniach.setText("Linie Produkcyjne");
        root.getChildren().add(infoOLiniach);
    }

    //funkcja tworząca magazyny
    public void utworzMagazyny(int pojemnoscMagazynu, AnchorPane root) {
        int rozmY = ((pojemnoscMagazynu / 10) * 15) + 5;

        for (int i = 0; i < 2; i++) {
            Rectangle magazyn;
            magazyn = new Rectangle(210 + (i * 185), 50, 155, rozmY);
            magazyn.setFill(Color.GREY);
            magazyn.setStroke(Color.BLACK);
            magazyn.setStrokeWidth(1);
            magazyn.setArcHeight(8);
            magazyn.setArcWidth(8);
            root.getChildren().add(magazyn);

            Text infoOMag = new Text();
            infoOMag.setX(210 + (i * 185));
            infoOMag.setY(40);
            infoOMag.setText("Magazyn " + i);
            root.getChildren().add(infoOMag);
        }

        Rectangle magazyn3;
        magazyn3 = new Rectangle(310, 430, 155, rozmY);
        magazyn3.setFill(Color.SEAGREEN);
        magazyn3.setStroke(Color.BLACK);
        magazyn3.setStrokeWidth(1);
        magazyn3.setArcHeight(8);
        magazyn3.setArcWidth(8);
        root.getChildren().add(magazyn3);

    }

    public static void main(String[] args) {
        launch(args);
    }
}