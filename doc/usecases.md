Przypadki użycia systemu:

1. Użytkownik może aktywować widok. Wówczas tiler minimalizuje wszystkie inne okna i pokazuje jedynie okna, zapisane w widoku zgodnie z layoutem.
2. Aktywacja widoku może odbyć się na różne sposoby. Zazwyczaj skrótem klawiszowym.
3. Stworzenie nowego okna powoduje dodanie go do aktualnego widoku.
4. Przywrócenie zminimalizowanego okna powoduje dodanie go do aktualnego widoku.
5. Zminimalizowanie okna powoduje usunięcie go z aktualnego widoku.
6. Zmaksymalizowanie okna powoduje powiększenie na pełny ekran (zgodnie z domyslnym zachowaniem) jednak po cofnięciu maksymalizacji okno powinno wrocić na swoje poprzednie miejsce.
7. Przesunięcie okna myszką na inne okno powinno zamienić te okna miejscami.
8. Użytkownik może pewne okna "wyłaczyć" z zarzadzania.
   1. Czy okno powinno być przywracane razem z widokiem ale bez układania czy kompletnie ignorowane? (być może widoki powinny mieć jedynie zapisane pozycje konkretnych okien, a układanie to zupełnie inny moduł, który operuje już na widoku - czyli zestawie okien)
9. Jedno okno może występować w wielu widokach. Możliwe jest np. ustawienie chrome i zooma oraz całkiem niezależnie chrome i IDE.
10. Tiler powinien automatycznie dbać o to, żeby okna były w odpowiednich miejscach, ale w razie potrzeby użytkownik powinien móc ręcznie uruchomić proces układania okien.
11. Po uruchomieniu otwarte okna trafiają do domyślnego widoku i są od razu układane.