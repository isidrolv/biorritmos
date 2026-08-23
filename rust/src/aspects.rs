//! Define los siete aspectos del biorritmo, replicando lib/aspects.rb del
//! proyecto Ruby (mismos periodos, colores y trazos).

pub struct Aspect {
    pub key: &'static str,
    pub label: &'static str,
    pub period: f64,
    pub color: &'static str,
    pub dash: &'static str,
    pub group: &'static str,
}

pub const LIST: [Aspect; 7] = [
    Aspect {
        key: "fisico",
        label: "Físico",
        period: 23.0,
        color: "#1656c9",
        dash: "0",
        group: "basico",
    },
    Aspect {
        key: "emocional",
        label: "Emocional",
        period: 28.0,
        color: "#d32f2f",
        dash: "0",
        group: "basico",
    },
    Aspect {
        key: "intelectual",
        label: "Intelectual",
        period: 33.0,
        color: "#1f9254",
        dash: "0",
        group: "basico",
    },
    Aspect {
        key: "espiritual",
        label: "Espiritual",
        period: 53.0,
        color: "#7c3aed",
        dash: "7 4",
        group: "complementario",
    },
    Aspect {
        key: "conciencia",
        label: "Conciencia",
        period: 48.0,
        color: "#0891b2",
        dash: "1 4",
        group: "complementario",
    },
    Aspect {
        key: "intuicion",
        label: "Intuición",
        period: 38.0,
        color: "#d97706",
        dash: "9 3 2 3",
        group: "complementario",
    },
    Aspect {
        key: "estetica",
        label: "Estética",
        period: 43.0,
        color: "#a3195b",
        dash: "3 3",
        group: "complementario",
    },
];

pub fn basico() -> Vec<&'static Aspect> {
    LIST.iter().filter(|a| a.group == "basico").collect()
}

pub fn complementario() -> Vec<&'static Aspect> {
    LIST.iter()
        .filter(|a| a.group == "complementario")
        .collect()
}
