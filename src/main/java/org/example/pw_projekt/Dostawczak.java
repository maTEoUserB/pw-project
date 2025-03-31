package org.example.pw_projekt;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Dostawczak extends Thread {
    int pojMag1;
    int pojMag2;
    int polX;
    int polY;
    int[] iloscWMagazynie1;
    int[] iloscWMagazynie2;
    Rectangle[] recMag1;
    Rectangle[] recMag2;
    Semaphore chronK1;
    Semaphore chronK2;
    Semaphore chronP1;
    Semaphore pusty1;
    Semaphore pelny1;
    Semaphore pusty2;
    Semaphore pelny2;
    Rectangle auto;
    int czasPodjazdu;
    ImagePattern imageAuto;
    private final AnchorPane root;
    public Random random;

    Dostawczak(int pojMag1, int pojMag2, int polX, int polY, int czasPodjazdu, Semaphore chronK1, Semaphore chronK2, Semaphore chronP1, Semaphore pusty1, Semaphore pelny1, Semaphore pusty2, Semaphore pelny2, ImagePattern imageAuto, AnchorPane root, int[] iloscWMagazynie1, int[] iloscWMagazynie2, Rectangle[] recMag1, Rectangle[] recMag2) {
        this.pojMag1 = pojMag1;
        this.pojMag2 = pojMag2;
        this.polX = polX;
        this.polY = polY;
        this.czasPodjazdu = czasPodjazdu;
        this.chronK1 = chronK1;
        this.chronK2 = chronK2;
        this.chronP1 = chronP1;
        this.pusty1 = pusty1;
        this.pelny1 = pelny1;
        this.pusty2 = pusty2;
        this.pelny2 = pelny2;
        this.imageAuto = imageAuto;
        this.root = root;
        this.iloscWMagazynie1 = iloscWMagazynie1;
        this.iloscWMagazynie2 = iloscWMagazynie2;
        this.recMag1 = recMag1;
        this.recMag2 = recMag2;
        this.random = new Random();
    }


    TranslateTransition podjedz, odjedz;
    Rectangle recA, recB;
    static volatile int wiersz1 = 0, wiersz2 = 0, miejsceMag1 = 0, miejsceMag2 = 0;

    public void run() {

        //samochód (jako prostokąt)
        auto = new Rectangle(polX, polY, 70, 50);
        auto.setFill(Color.GREEN);
        auto.setArcHeight(8);
        auto.setArcWidth(8);
        auto.setFill(imageAuto);
        Platform.runLater(() -> root.getChildren().add(auto));

        while (true) {

            try {
                chronP1.acquire();
            }//synchronizacja pomiędzy autami
            catch (InterruptedException e) {
                break;
            }

            System.out.println("DOSTAWCZAK Stan magazynu 1: " + this.iloscWMagazynie1[0] + ", stan magazynu 2: " + this.iloscWMagazynie2[0]);
            if (this.iloscWMagazynie1[0] <= 0.1 * pojMag1 || this.iloscWMagazynie2[0] <= 0.15 * pojMag2) {

                System.out.println("Dostawczak będzie uzupełniał!!!!!");

                //podjazd do magazynów
                Platform.runLater(() -> {
                    podjedz = new TranslateTransition();
                    podjedz.setDuration(Duration.seconds(czasPodjazdu));
                    podjedz.setToX(-200);
                    podjedz.setToY(50 - auto.getY());
                    podjedz.setCycleCount(1);
                    podjedz.setNode(auto);
                    podjedz.play();
                });

                try {
                    sleep(czasPodjazdu * 1000L); //czekaj na dojazd
                    chronK1.acquire();
                } catch (InterruptedException e) {
                    break;
                }

                // Aktualizacja ilości w magazynach
                int dodaneDoMagazynu1 = pojMag1 - this.iloscWMagazynie1[0];
                this.iloscWMagazynie1[0] = pojMag1;

                //załadunek do magazyn1
                wiersz1 = miejsceMag1 / 10;
                for (int i = 1; i <= dodaneDoMagazynu1; i++) {
                    recA = new Rectangle(580, 50, 10, 10);
                    recA.setFill(Color.GREEN);
                    recA.setArcHeight(8);
                    recA.setArcWidth(8);
                    Platform.runLater(() -> root.getChildren().add(recA));

                    int offsetX = 215 + (15 * (miejsceMag1 % 10));
                    int offsetY = 55 + (15 * wiersz1);

                    TranslateTransition umiesc1 = new TranslateTransition();
                    umiesc1.setDuration(Duration.seconds(0.05));
                    umiesc1.setToX(offsetX - recA.getX());
                    umiesc1.setToY(offsetY - recA.getY());
                    umiesc1.setCycleCount(1);
                    umiesc1.setNode(recA);
                    umiesc1.play();
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                    recMag1[miejsceMag1] = recA;
                    if (miejsceMag1 % 10 == 9) wiersz1++;
                    miejsceMag1 = (miejsceMag1 + 1) % pojMag1;
                    if (miejsceMag1 == 0) wiersz1 = 0;

                    pelny1.release();
                }

                chronK1.release();

                //produkcja2
                try {
                    chronK2.acquire();
                } catch (InterruptedException e) {
                    break;
                }

                int dodaneDoMagazynu2 = pojMag2 - this.iloscWMagazynie2[0];
                this.iloscWMagazynie2[0] = pojMag2;

                //załadunek do magazynu 2
                wiersz2 = miejsceMag2 / 10;
                for (int i = 1; i <= dodaneDoMagazynu2; i++) {
                    recB = new Rectangle(580, 50, 10, 10);
                    recB.setFill(Color.WHEAT);
                    recB.setArcHeight(8);
                    recB.setArcWidth(8);
                    Platform.runLater(() -> root.getChildren().add(recB));

                    int offsetX = 400 + (15 * (miejsceMag2 % 10));
                    int offsetY = 55 + (15 * wiersz2);

                    TranslateTransition umiesc2 = new TranslateTransition();
                    umiesc2.setDuration(Duration.seconds(0.05));
                    umiesc2.setToX(offsetX - recB.getX());
                    umiesc2.setToY(offsetY - recB.getY());
                    umiesc2.setCycleCount(1);
                    umiesc2.setNode(recB);
                    umiesc2.play();
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                    recMag2[miejsceMag2] = recB;
                    if (miejsceMag2 % 10 == 9) wiersz2++;
                    miejsceMag2 = (miejsceMag2 + 1) % pojMag2;
                    if (miejsceMag2 == 0) wiersz2 = 0;

                    pelny2.release();
                }

                chronK2.release();

                //odjazd z magazynów
                Platform.runLater(() -> {
                    odjedz = new TranslateTransition();
                    odjedz.setDuration(Duration.seconds(2));
                    odjedz.setToX(0);
                    odjedz.setToY(polY - auto.getY());
                    odjedz.setCycleCount(1);
                    odjedz.setNode(auto);
                    odjedz.play();
                });

                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    break;
                } //czekaj aż odjedzie

            } else {
                System.out.println("Dostawczak NIE będzie uzupełniał!!!");
            }

            chronP1.release(); //synchronizacja pomiędzy autami
        }
    }


}
