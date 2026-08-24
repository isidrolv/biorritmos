from datetime import date
import math
import unittest

from aspects import ASPECTS
from biorhythm import CHART_HEIGHT, CHART_WIDTH, MARGIN, format_short, phase_label, series, value_at


class BiorhythmTests(unittest.TestCase):
    def test_value_is_zero_on_birth_date(self):
        for aspect in ASPECTS:
            self.assertAlmostEqual(value_at(0, aspect["period"]), 0.0)

    def test_quarter_cycle_is_peak(self):
        self.assertAlmostEqual(value_at(23 / 4, 23), 100.0)

    def test_phase_labels(self):
        self.assertEqual(phase_label(2.99, -5), "Crítico")
        self.assertEqual(phase_label(30, 31), "Ascendente")
        self.assertEqual(phase_label(30, 29), "Descendente")

    def test_series_has_same_geometry_and_all_aspects(self):
        result = series(date(2000, 1, 1), date(2026, 8, 23), today=date(2026, 8, 23))
        self.assertEqual(len(result["lines"]), 7)
        self.assertEqual(len(result["lines"][0]["points"]), 31)
        self.assertEqual(len(result["date_labels"]), 7)
        self.assertEqual(result["marker_label"], "Hoy")
        expected_center = MARGIN["left"] + (CHART_WIDTH - MARGIN["left"] - MARGIN["right"]) / 2
        self.assertEqual(result["center_x"], expected_center)
        self.assertEqual(result["gridlines"][0]["y"], MARGIN["top"])
        self.assertEqual(result["gridlines"][-1]["y"], CHART_HEIGHT - MARGIN["bottom"])

    def test_non_today_marker_and_span(self):
        result = series(date(1990, 5, 3), date(2024, 12, 7), today=date(2026, 8, 23))
        self.assertFalse(result["is_today"])
        self.assertEqual(result["marker_label"], "07 dic")
        self.assertEqual(result["date_labels"][0]["label"], "22 nov")
        self.assertEqual(result["date_labels"][-1]["label"], "22 dic")

    def test_spanish_date_format(self):
        self.assertEqual(format_short(date(2026, 8, 3)), "03 ago")


if __name__ == "__main__":
    unittest.main()
