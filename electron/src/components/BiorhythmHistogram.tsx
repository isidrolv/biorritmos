import type {Aspect, AspectKey} from "../setup-datasets.ts";
import {CHART_HEIGHT, CHART_WIDTH, MARGIN} from "../variables.ts";
import BiorhythmAspectsSwitchBoard from "./components/BiorhythmAspectsSwitchBoard.tsx";

export type BiorhythmLine = {
    aspect: Aspect;
    path: string;
    currentValue: number;
    status: string;
    markerX: number;
    markerY: number
};

export type BiorhythmSeries = {
        lines: BiorhythmLine[];
        xScale: (index: number) => number;
        yScale: (value: number) => number;
        dateLabels: { offset: number; i: number; date: Date }[];
        centerX: number
}

export default function BiorhythmHistogram(props: {
    series: BiorhythmSeries,
    today: boolean,
    selectedDate: Date,
    visible: Record<AspectKey, boolean>,
    onToggleAspect: (key: AspectKey) => void
}) {
    return <>
        <svg
            className="chart"
            viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`}
            role="img"
            aria-label="Gráfico de biorritmo con ciclos físico, emocional, intelectual, espiritual, conciencia, intuición y estética"
        >
            {[100, 50, 0, -50, -100].map((value) => <g key={value}>
                <line x1={MARGIN.left} x2={CHART_WIDTH - MARGIN.right} y1={props.series.yScale(value)}
                      y2={props.series.yScale(value)} className={value === 0 ? 'gridline gridline-zero' : 'gridline'}/>
                <text x={MARGIN.left - 8} y={props.series.yScale(value)} className="axis-label"
                      textAnchor="end" dy="0.32em">{value}</text>
            </g>)}

            <line
                x1={props.series.centerX}
                x2={props.series.centerX}
                y1={MARGIN.top}
                y2={CHART_HEIGHT - MARGIN.bottom}
                className="marker-line"
            />
            <text x={props.series.centerX} y={MARGIN.top - 4} className="marker-label" textAnchor="middle">
                {props.today ? 'Hoy' : props.selectedDate.toLocaleDateString('es-ES', {
                    day: '2-digit',
                    month: 'short'
                })}
            </text>

            {props.series.dateLabels.map(({offset, i, date}) => <text key={offset} x={props.series.xScale(i)}
                                                                      y={CHART_HEIGHT - MARGIN.bottom + 18}
                                                                      className="axis-label" textAnchor="middle">
                {date.toLocaleDateString('es-ES', {day: '2-digit', month: 'short'})}
            </text>)}

            {props.series.lines
                .filter((line) => props.visible[line.aspect.key])
                .map((line) => <g key={line.aspect.key}>
                    <path d={line.path} fill="none" stroke={line.aspect.color} strokeWidth={2}
                          strokeDasharray={line.aspect.dash} strokeLinejoin="round" strokeLinecap="round"/>
                    <circle cx={line.markerX} cy={line.markerY} r={5} fill={line.aspect.color}
                            stroke="#ffffff" strokeWidth={2}>
                        <title>{line.aspect.label}: {Math.round(line.currentValue)}%</title>
                    </circle>
                </g>)}
        </svg>

        <BiorhythmAspectsSwitchBoard
            lines={props.series.lines}
            visible={props.visible}
            onToggleAspect={props.onToggleAspect}
        />
    </>
}
