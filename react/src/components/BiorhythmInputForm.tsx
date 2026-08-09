export default function BiorhythmInputForm(props: {
    todayStr: string,
    birthDate: string,
    selectedDate: string,
    isToday: boolean,
    onBirthDateChange: (value: string) => void,
    onSelectedDateChange: (value: string) => void,
    onPreviousDay: () => void,
    onToday: () => void,
    onNextDay: () => void
}) {
    return <div className="controls">
        <label>
            Fecha de nacimiento
            <input
                type="date"
                value={props.birthDate}
                max={props.todayStr}
                onChange={(event) => props.onBirthDateChange(event.target.value)}
            />
        </label>
        <label>
            Fecha a analizar
            <input
                type="date"
                value={props.selectedDate}
                onChange={(event) => props.onSelectedDateChange(event.target.value)}
            />
        </label>
        <div className="date-nav">
            <button type="button" onClick={props.onPreviousDay} aria-label="Día anterior">
                ←
            </button>
            <button type="button" onClick={props.onToday} disabled={props.isToday}>
                Hoy
            </button>
            <button type="button" onClick={props.onNextDay} aria-label="Día siguiente">
                →
            </button>
        </div>
    </div>
}
