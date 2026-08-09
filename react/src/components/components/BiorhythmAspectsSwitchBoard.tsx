import type {AspectKey} from "../../setup-datasets.ts";
import type {BiorhythmLine} from "../BiorhythmHistogram.tsx";

type AspectGroupKey = 'basico' | 'complementario'

type SwitchBoardProps = {
    lines: BiorhythmLine[]
    visible: Record<AspectKey, boolean>
    onToggleAspect: (key: AspectKey) => void
}

function AspectSwitch({
    line,
    visible,
    onToggle,
}: {
    line: BiorhythmLine
    visible: boolean
    onToggle: (key: AspectKey) => void
}) {
    return (
        <li>
            <button
                type="button"
                className="legend-item"
                aria-pressed={visible}
                onClick={() => onToggle(line.aspect.key)}
            >
                <span
                    className="swatch"
                    style={{
                        background: visible ? line.aspect.color : 'transparent',
                        borderColor: line.aspect.color,
                    }}
                />
                <span className="legend-text">{line.aspect.label}</span>
                <span className="legend-value">{Math.round(line.currentValue)}%</span>
                <span className="legend-status">{line.status}</span>
            </button>
        </li>
    )
}

function AspectGroup({group, lines, visible, onToggleAspect}: SwitchBoardProps & {group: AspectGroupKey}) {
    const groupLines = lines.filter((line) => line.aspect.group === group)

    return (
        <div className="legend-group">
            <h2>{group === 'basico' ? 'Aspectos básicos' : 'Aspectos complementarios'}</h2>
            <ul>
                {groupLines.map((line) => (
                    <AspectSwitch
                        key={line.aspect.key}
                        line={line}
                        visible={visible[line.aspect.key]}
                        onToggle={onToggleAspect}
                    />
                ))}
            </ul>
        </div>
    )
}

export default function BiorhythmAspectsSwitchBoard(props: SwitchBoardProps) {
    return <div className="legend">
        <AspectGroup group="basico" {...props}/>
        <AspectGroup group="complementario" {...props}/>
    </div>
}
