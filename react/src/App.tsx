import {useMemo, useState} from 'react'
import {toInputValue, fromInputValue, addDays, daysBetween, biorhythmValue, phaseLabel} from './utils'
import './App.css'
import {type AspectKey, ASPECTS} from './setup-datasets.ts'
import {BiorhythmHeader} from './components/BiorhythmHeader.tsx'
import BiorhythmInputForm from './components/BiorhythmInputForm.tsx'
import {MARGIN, PLOT_HEIGHT, PLOT_WIDTH, RANGE_DAYS} from './variables.ts'
import BiorhythmResults from './components/BiorhythmResults.tsx'

const todayStr = toInputValue(new Date())

function App() {
    const [birthDateStr, setBirthDateStr] = useState('')
    const [selectedDateStr, setSelectedDateStr] = useState(todayStr)
    const [visible, setVisible] = useState<Record<AspectKey, boolean>>(() =>
        ASPECTS.reduce((result, aspect) => ({...result, [aspect.key]: true}), {} as Record<AspectKey, boolean>),
    )

    const birthDate = useMemo(() => fromInputValue(birthDateStr), [birthDateStr])
    const selectedDate = useMemo(
        () => fromInputValue(selectedDateStr) ?? new Date(),
        [selectedDateStr],
    )

    const toggleAspect = (key: AspectKey) => {
        setVisible((previous) => ({...previous, [key]: !previous[key]}))
    }

    const shiftSelectedDate = (amount: number) => {
        setSelectedDateStr(toInputValue(addDays(selectedDate, amount)))
    }

    const series = useMemo(() => {
        if (!birthDate) return null

        const offsets = Array.from({length: RANGE_DAYS * 2 + 1}, (_, index) => index - RANGE_DAYS)
        const dates = offsets.map((offset) => addDays(selectedDate, offset))
        const daysSinceBirth = dates.map((date) => daysBetween(birthDate, date))
        const xScale = (index: number) => MARGIN.left + (index / (offsets.length - 1)) * PLOT_WIDTH
        const yScale = (value: number) => MARGIN.top + (1 - (value + 100) / 200) * PLOT_HEIGHT

        const lines = ASPECTS.map((aspect) => {
            const values = daysSinceBirth.map((days) => biorhythmValue(days, aspect.period))
            const path = values
                .map((value, index) => `${index === 0 ? 'M' : 'L'} ${xScale(index).toFixed(2)} ${yScale(value).toFixed(2)}`)
                .join(' ')
            const currentValue = values[RANGE_DAYS]
            const nextValue = biorhythmValue(daysSinceBirth[RANGE_DAYS] + 1, aspect.period)

            return {
                aspect,
                path,
                currentValue,
                status: phaseLabel(currentValue, nextValue),
                markerX: xScale(RANGE_DAYS),
                markerY: yScale(currentValue),
            }
        })

        const dateLabels = offsets
            .map((offset, index) => ({offset, i: index, date: dates[index]}))
            .filter(({offset}) => offset % 5 === 0)

        return {lines, xScale, yScale, dateLabels, centerX: xScale(RANGE_DAYS)}
    }, [birthDate, selectedDate])

    const isToday = selectedDateStr === todayStr

    return (
        <section id="biorritmo">
            <BiorhythmHeader/>
            <BiorhythmInputForm
                todayStr={todayStr}
                birthDate={birthDateStr}
                selectedDate={selectedDateStr}
                isToday={isToday}
                onBirthDateChange={setBirthDateStr}
                onSelectedDateChange={setSelectedDateStr}
                onPreviousDay={() => shiftSelectedDate(-1)}
                onToday={() => setSelectedDateStr(todayStr)}
                onNextDay={() => shiftSelectedDate(1)}
            />
            <BiorhythmResults
                series={series}
                today={isToday}
                selectedDate={selectedDate}
                visible={visible}
                onToggleAspect={toggleAspect}
            />
        </section>
    )
}

export default App
