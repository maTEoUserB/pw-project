package org.example.pw_projekt;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.Semaphore;


public class LiniaProdukcyjna extends Thread {
    int pojMag1;
    int pojMag2;
    int pojMag3;
    int polX;
    int polY;
    static volatile int k1 = 0;
    static volatile int k2 = 0;
    static volatile int p3 = 0;
    int[] iloscWMagazynie1;
    int[] iloscWMagazynie2;
    int[] iloscWMagazynie3;
    Rectangle[] recMag1;
    Rectangle[] recMag2;
    Rectangle[] recMag3;
    Semaphore chronK1;
    Semaphore pusty1;
    Semaphore pelny1;
    Semaphore chronK2;
    Semaphore pusty2;
    Semaphore pelny2;
    Semaphore chronP3;
    Semaphore pusty3;
    Semaphore pelny3;
    private final AnchorPane root;
    public Random random;
    int losCzas;
    int time;
    final static Object lock = new Object();


    LiniaProdukcyjna(int time, int pojMag1, int pojMag2, int pojMag3, Rectangle[] recMag3, AnchorPane root, int polX, int polY, Semaphore chronK1, Semaphore pusty1, Semaphore pelny1, Semaphore chronK2, Semaphore pusty2, Semaphore pelny2, Semaphore pusty3, Semaphore pelny3, Semaphore chronP3, int[] iloscWMagazynie1, int[] iloscWMagazynie2, int[] iloscWMagazynie3, Rectangle[] recMag1, Rectangle[] recMag2) {
        this.time = time;
        this.pojMag1 = pojMag1;
        this.pojMag2 = pojMag2;
        this.pojMag3 = pojMag3;
        this.recMag3 = recMag3;
        this.polX = polX;
        this.polY = polY;
        this.chronK1 = chronK1;
        this.pusty1 = pusty1;
        this.pelny1 = pelny1;
        this.chronK2 = chronK2;
        this.pusty2 = pusty2;
        this.pelny2 = pelny2;
        this.chronP3 = chronP3;
        this.pusty3 = pusty3;
        this.pelny3 = pelny3;
        this.iloscWMagazynie1 = iloscWMagazynie1;
        this.iloscWMagazynie2 = iloscWMagazynie2;
        this.iloscWMagazynie3 = iloscWMagazynie3;
        this.recMag1 = recMag1;
        this.recMag2 = recMag2;
        this.root = root;
        this.random = new Random();
        this.losCzas = random.nextInt(10) + 1;
    }


    TranslateTransition pobierz1, pobierz2, wlozDo3;
    Rectangle recA, recB;

    public void run() {
        while (true) {

            try {
                pelny1.acquire();
                chronK1.acquire();
                sleep((long) losCzas * time * 100);
            } catch (InterruptedException e) {
                break;
            }

            //pobierz z magazynu 1
            recA = recMag1[k1];
            recMag1[k1] = null;
            Platform.runLater(() -> {
                pobierz1 = new TranslateTransition();
                pobierz1.setDuration(Duration.seconds(1));
                pobierz1.setToX((this.polX + 10) - recA.getX());
                pobierz1.setToY((this.polY + 20) - recA.getY());
                pobierz1.setCycleCount(1);
                pobierz1.setNode(recA);
                pobierz1.play();
            });


            k1 = (k1 + 1) % pojMag1;
            this.iloscWMagazynie1[0]--;
            chronK1.release();

            try {
                pelny2.acquire();
                chronK2.acquire();
                sleep((long)losCzas * time * 100);
            } catch (InterruptedException e) {
                break;
            }

            //pobierz z magazynu 2
            recB = recMag2[k2];
            recMag2[k2] = null;
            Platform.runLater(() -> {
                pobierz2 = new TranslateTransition();
                pobierz2.setDuration(Duration.seconds(1));
                pobierz2.setToX((this.polX + 10) - recB.getX());
                pobierz2.setToY((this.polY + 50) - recB.getY());
                pobierz2.setCycleCount(1);
                pobierz2.setNode(recB);
                pobierz2.play();
            });


            k2 = (k2 + 1) % pojMag2;
            this.iloscWMagazynie2[0]--;
            chronK2.release();

            try {
                sleep(losCzas * 1000L);
            } catch (InterruptedException e) {
                break;
            } //produkcja trwa losowy czas

            // produkcja (zmiana materiałów na produkt)
            Platform.runLater(() -> {
                root.getChildren().remove(recA);
                root.getChildren().remove(recB);
            });

            Rectangle produkt = new Rectangle(polX + 10, polY + 80, 10, 10);
            produkt.setFill(Color.GREEN);
            produkt.setArcHeight(8);
            produkt.setArcWidth(8);
            produkt.setFill(Color.ROSYBROWN);
            Platform.runLater(() -> root.getChildren().add(produkt));

            try {
                pusty3.acquire();
                chronP3.acquire();
            } catch (InterruptedException e) {
                break;
            }


            int miejsceWMagazynie;
            // synchronizacja dostepu do zmiennej p3
            synchronized (lock) {
                miejsceWMagazynie = p3;
                p3 = (p3 + 1) % pojMag3;
            }

            //włóż do magazynu 3
            final int finalMiejsceWMagazynie = miejsceWMagazynie; // zmienna do użytku w Platform.runLater
            Platform.runLater(() -> {
                if (root.getChildren().contains(produkt)) {
                    wlozDo3 = new TranslateTransition();
                    wlozDo3.setDuration(Duration.seconds(0.5));
                    wlozDo3.setToX(315 + (15 * (finalMiejsceWMagazynie % 10)) - produkt.getX());
                    wlozDo3.setToY(435 + (15 * (finalMiejsceWMagazynie / 10)) - produkt.getY());
                    wlozDo3.setCycleCount(1);
                    wlozDo3.setNode(produkt);
                    wlozDo3.play();
                    recMag3[miejsceWMagazynie] = produkt;
                    this.iloscWMagazynie3[0]++;

                } else {
                    System.err.println("Produkt jest null lub nie znajduje się w drzewie sceny.");
                }
            });

            chronP3.release();
        }
    }

}
