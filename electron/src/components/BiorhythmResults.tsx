import type {AspectKey} from "../setup-datasets.ts";
import BiorhythmHistogram, {type BiorhythmSeries} from "./BiorhythmHistogram.tsx";

export default function BiorhythmResults(props: {
    series: BiorhythmSeries | null,
    today: boolean,
    selectedDate: Date,
    visible: Record<AspectKey, boolean>,
    onToggleAspect: (key: AspectKey) => void
}) {
    return <>
        {!props.series ? (
            <p className="empty-state">Ingresa tu fecha de nacimiento para ver tu gráfico de biorritmo.</p>
        ) : (
            <BiorhythmHistogram series={props.series} today={props.today} selectedDate={props.selectedDate}
                                visible={props.visible} onToggleAspect={props.onToggleAspect}/>
        )}
    </>
}
